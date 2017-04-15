/*
 * Copyright © 2015 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.raml.doc;

import guru.nidi.loader.Loader;
import guru.nidi.loader.basic.CachingLoaderInterceptor;
import guru.nidi.loader.basic.FileLoader;
import guru.nidi.loader.basic.InterceptingLoader;
import guru.nidi.loader.basic.UriLoader;
import guru.nidi.loader.use.raml.RamlLoad;
import guru.nidi.raml.doc.st.Feature;
import guru.nidi.raml.doc.st.Generator;
import org.raml.model.Raml;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GeneratorConfig {
    private final List<String> ramlLocations;
    private final File target;
    private final EnumSet<Feature> features;
    private final String baseUri;
    private final String baseUriParameters;
    private final Loader customization;
    private final boolean forceDelete;
    private final ResourceCache resourceCache;

    public GeneratorConfig(List<String> ramlLocations, File target, EnumSet<Feature> features, String baseUri, String baseUriParameters, Loader customization, boolean forceDelete) {
        this.ramlLocations = ramlLocations;
        this.target = target;
        this.features = features;
        this.baseUri = baseUri;
        this.baseUriParameters = baseUriParameters;
        this.customization = customization;
        this.forceDelete = forceDelete;
        resourceCache = new ResourceCache(new File(target, "@resource"));
    }

    public File getTarget() {
        return target;
    }

    public boolean hasFeature(Feature feature) {
        return features.contains(feature);
    }

    public String getBaseUri() {
        return baseUri;
    }

    public ResourceCache getResourceCache() {
        return resourceCache;
    }

    public boolean isForceDelete() {
        return forceDelete;
    }

    public InputStream loadCustomization(String name) {
        if (customization == null) {
            throw new Loader.ResourceNotFoundException(name);
        }
        return customization.fetchResource(name, -1);
    }

    public static String getBaseOfRaml(String ramlLocation) {
        final int pos = ramlLocation.lastIndexOf('/');
        //when there's no / in the raml location, suppose it's a filename
        return pos < 0 ? "./" : ramlLocation.substring(0, pos);
    }

    public String getBaseUri(Raml raml) {
        if (baseUri != null) {
            return baseUri;
        }
        String baseUri = raml.getBaseUri().replace("{version}", raml.getVersion());
        if (baseUriParameters != null) {
            for (final String param : baseUriParameters.split(",")) {
                final String[] keyValue = param.split("=");
                if (keyValue.length != 2) {
                    throw new IllegalArgumentException("baseUriParameters must be of the form 'key1=value1,key2=value2,...' but is '" + baseUriParameters + "'");
                }
                baseUri = baseUri.replace("{" + keyValue[0] + "}", keyValue[1]);
            }
        }
        if (baseUri.contains("{")) {
            throw new IllegalArgumentException("Unresolved baseUri: '" + baseUri + "'. Use 'baseUri' or 'baseUriParameters' parameters to specify it.");
        }
        return baseUri;
    }

    private LoadResults loadRamls() throws IOException {
        final LoadResults res = new LoadResults();
        for (String loc : ramlLocations) {
            try {
                final SavingLoaderInterceptor sli = new SavingLoaderInterceptor();
                final InterceptingLoader loader = new InterceptingLoader(new UriLoader(new FileLoader(new File("."))), sli);
                final Raml raml = new RamlLoad(loader).load(loc);
                new SchemaLoader(raml, loc, loader).loadSchemas();
                sli.raml = raml;
                res.ramls.add(raml);
                res.slis.add(sli);
            } catch (Exception e) {
                throw new IOException("Problem loading RAML from '" + loc + "'", e);
            }
        }
        return res;
    }

    public String generate() throws IOException {
        final LoadResults loadResults = loadRamls();
        fillResourceCache(loadResults);

        final Generator generator = new Generator(this);
        generator.generate(loadResults.ramls);

        writeResources(loadResults, generator);
        return generator.getTarget(loadResults.ramls.get(0)).getName();
    }

    private void fillResourceCache(LoadResults loadResults) throws UnsupportedEncodingException {
        for (final SavingLoaderInterceptor sli : loadResults.slis) {
            for (final Map.Entry<String, byte[]> entry : sli.data.entrySet()) {
                final String key = sli.relativizePath(entry.getKey());
                resourceCache.cache(sli.raml, key, new String(entry.getValue(), "utf-8"));
            }
        }
    }

    private void writeResources(LoadResults loadResults, Generator generator) throws IOException {
        for (final SavingLoaderInterceptor sli : loadResults.slis) {
            sli.writeDataToZip(generator);
        }
        resourceCache.flush();
    }

    public static String safeName(Raml raml) {
        return IoUtil.safeName(raml.getTitle());
    }

    private static class LoadResults {
        final List<Raml> ramls = new ArrayList<>();
        final List<SavingLoaderInterceptor> slis = new ArrayList<>();
    }

    private static class SavingLoaderInterceptor extends CachingLoaderInterceptor {
        private Raml raml;
        private String ramlPath;
        private final Map<String, byte[]> data = new HashMap<>();

        @Override
        protected void processLoaded(String name, byte[] data) {
            final int pos = name.indexOf("://");
            final String path = pos < 0 ? name : name.substring(pos + 3);
            this.data.put(path, data);
            //raml is the first resource to be loaded
            if (ramlPath == null) {
                ramlPath = path;
            }
        }

        public String relativizePath(String path) {
            int pos = ramlPath.lastIndexOf('/');
            return path.startsWith(ramlPath.substring(0, pos)) ? path.substring(pos + 1) : path;
        }

        public void writeDataToFiles(File dir) throws IOException {
            dir.mkdirs();
            for (Map.Entry<String, byte[]> d : data.entrySet()) {
                final String path = IoUtil.normalizePath(d.getKey());
                try (final InputStream in = new ByteArrayInputStream(d.getValue());
                     final OutputStream out = new FileOutputStream(new File(dir, path))) {
                    byte[] buf = new byte[10000];
                    int read;
                    while ((read = in.read(buf)) > 0) {
                        out.write(buf, 0, read);
                    }
                }
            }
        }

        public void writeDataToZip(Generator generator) throws IOException {
            writeDataToZip(new File(generator.getTarget(raml), safeName(raml) + ".zip"));
        }

        public void writeDataToZip(File file) throws IOException {
            file.getParentFile().mkdirs();
            try (final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file))) {
                for (Map.Entry<String, byte[]> d : data.entrySet()) {
                    final String path = IoUtil.normalizePath(d.getKey());
                    out.putNextEntry(new ZipEntry(path));
                    try (final InputStream in = new ByteArrayInputStream(d.getValue())) {
                        byte[] buf = new byte[10000];
                        int read;
                        while ((read = in.read(buf)) > 0) {
                            out.write(buf, 0, read);
                        }
                    }
                    out.closeEntry();
                }
            }
        }
    }
}