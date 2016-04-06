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

import org.raml.model.Raml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ResourceCache {
    private final Map<String, String> schemas = new HashMap<>();
    private final File basedir;
    private int currentKey = 0;

    public ResourceCache(File basedir) {
        this.basedir = basedir;
    }

    public String cache(Raml raml, String schema) {
        String key = findSchema(raml, schema);
        if (key == null) {
            key = cache(raml, (++currentKey) + ".inline", schema);
        }
        return key;
    }

    public String cache(Raml raml, String key, String schema) {
        final String fullKey = key(raml, key);
        schemas.put(fullKey, schema);
        return fullKey;
    }

    private String findSchema(Raml raml, String schema) {
        for (final Map.Entry<String, String> entry : schemas.entrySet()) {
            if (entry.getKey().startsWith(key(raml, "")) && entry.getValue().equals(schema)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private String key(Raml raml, String suffix) {
        return GeneratorConfig.safeName(raml) + "/" + suffix;
    }

    public String schema(String key) {
        String schema = schemas.get(key);
        if (schema == null) {
            int pos = key.lastIndexOf('/');
            if (pos > 0) {
                schema = schemas.get(key.substring(pos + 1));
            }
        }
        return schema;
    }

    public void flush() throws IOException {
        for (final Map.Entry<String, String> entry : schemas.entrySet()) {
            saveSchema(entry.getKey(), entry.getValue());
        }
    }

    protected void saveSchema(String key, String schema) throws IOException {
        final File file = new File(basedir, key);
        file.getParentFile().mkdirs();
        try (final InputStream in = new ByteArrayInputStream(schema.getBytes("utf-8"));
             final OutputStream out = new FileOutputStream(file)) {
            IoUtil.copy(in, out);
        }
    }

}