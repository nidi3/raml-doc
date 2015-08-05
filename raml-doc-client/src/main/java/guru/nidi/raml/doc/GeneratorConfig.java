/*
 * Copyright (C) 2015 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.raml.doc;

import guru.nidi.loader.basic.CachingLoaderInterceptor;
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

/**
 *
 */
public class GeneratorConfig {
    private final String ramlLocations;
    private final File target;
    private final EnumSet<Feature> features;
    private final String parentTitle;
    private final String baseUri;
    private final String baseUriParameters;

    public GeneratorConfig(String ramlLocations, File target, EnumSet<Feature> features, String parentTitle, String baseUri, String baseUriParameters) {
        this.ramlLocations = ramlLocations;
        this.target = target;
        this.features = features;
        this.parentTitle = parentTitle;
        this.baseUri = baseUri;
        this.baseUriParameters = baseUriParameters;
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

    private List<Raml> loadRamls(Generator generator) throws IOException {
        final List<Raml> ramls = new ArrayList<>();
        for (String loc : ramlLocations.split(",")) {
            try {
                final SavingLoaderInterceptor sli = new SavingLoaderInterceptor();
                final Raml raml = new RamlLoad(new InterceptingLoader(new UriLoader(), sli)).load(loc);
                ramls.add(raml);
//            sli.writeDataToFiles(new File(getEffectiveTarget(), "raml"));
                sli.writeDataToZip(new File(generator.getTarget(raml), raml.getTitle() + ".zip"));
            } catch (Exception e) {
                throw new IOException("Problem loading RAML from '" + loc + "'", e);
            }
        }
        return ramls;
    }

    public void generate() throws IOException {
        final Generator generator = new Generator(target).features(this.features);
        final List<Raml> ramls = loadRamls(generator);
        for (final Raml raml : ramls) {
            generator
                    .parentTitle(parentTitle)
                    .baseUri(getBaseUri(raml))
                    .generate(raml, ramls);
        }
    }


    private static class SavingLoaderInterceptor extends CachingLoaderInterceptor {
        private final Map<String, byte[]> data = new HashMap<>();

        @Override
        protected void processLoaded(String name, byte[] data) {
            final int pos = name.indexOf("://");
            this.data.put(pos < 0 ? name : name.substring(pos + 3), data);
        }

        public void writeDataToFiles(File dir) throws IOException {
            dir.mkdirs();
            for (Map.Entry<String, byte[]> d : data.entrySet()) {
                try (final InputStream in = new ByteArrayInputStream(d.getValue());
                     final OutputStream out = new FileOutputStream(new File(dir, d.getKey()))) {
                    byte[] buf = new byte[10000];
                    int read;
                    while ((read = in.read(buf)) > 0) {
                        out.write(buf, 0, read);
                    }
                }
            }
        }

        public void writeDataToZip(File file) throws IOException {
            file.getParentFile().mkdirs();
            try (final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file))) {
                for (Map.Entry<String, byte[]> d : data.entrySet()) {
                    out.putNextEntry(new ZipEntry(d.getKey()));
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