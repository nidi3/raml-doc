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
import guru.nidi.raml.doc.st.Generator;
import org.raml.model.Raml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class GeneratorConfig {
    private final String ramlLocation;
    private final File target;
    private final boolean tryOut;
    private final String baseUri;
    private final String baseUriParameters;
    private Raml raml;

    public GeneratorConfig(String ramlLocation, File target, boolean tryOut, String baseUri, String baseUriParameters) {
        this.ramlLocation = ramlLocation;
        this.target = target;
        this.tryOut = tryOut;
        this.baseUri = baseUri;
        this.baseUriParameters = baseUriParameters;
    }

    public String getRamlLocation() {
        return ramlLocation;
    }

    public File getTarget() {
        return target;
    }

    public boolean isTryOut() {
        return tryOut;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public String getBaseUriParameters() {
        return baseUriParameters;
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

    public GeneratorConfig loadRaml() throws IOException {
        try {
            final SavingLoaderInterceptor sli = new SavingLoaderInterceptor();
            raml = new RamlLoad(new InterceptingLoader(new UriLoader(), sli)).load(ramlLocation);
            sli.writeData(new File(getEffectiveTarget(), "raml"));
            return this;
        } catch (Exception e) {
            throw new IOException("Problem loading RAML from '" + ramlLocation + "'", e);
        }
    }

    public void generate() throws IOException {
        new Generator()
                .tryOut(tryOut ? getBaseUri(raml) : null)
                .generate(raml, getEffectiveTarget());
    }

    public File getEffectiveTarget() {
        return new File(target, raml.getTitle());
    }

    private static class SavingLoaderInterceptor extends CachingLoaderInterceptor {
        private final Map<String, byte[]> data = new HashMap<>();

        @Override
        protected void processLoaded(String name, byte[] data) {
            final int pos = name.indexOf("://");
            this.data.put(pos < 0 ? name : name.substring(pos + 3), data);
        }

        public void writeData(File target) throws IOException {
            target.mkdirs();
            for (Map.Entry<String, byte[]> d : data.entrySet()) {
                try (final InputStream in = new ByteArrayInputStream(d.getValue());
                     final OutputStream out = new FileOutputStream(new File(target, d.getKey()))) {
                    byte[] buf = new byte[10000];
                    int read;
                    while ((read = in.read(buf)) > 0) {
                        out.write(buf, 0, read);
                    }
                }
            }
        }
    }
}