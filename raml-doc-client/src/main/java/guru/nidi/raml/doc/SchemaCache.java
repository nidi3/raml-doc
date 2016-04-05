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

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class SchemaCache {
    private final Map<String, String> schemas = new HashMap<>();
    private final Map<String, String> keys = new HashMap<>();
    private int currentKey = 0;

    public void cache(String key, String schema) {
        schemas.put(key, schema);
    }

    public String cache(String schema) {
        String key = keys.get(schema);
        if (key == null) {
            key = "" + (++currentKey);
            schemas.put(key, schema);
            keys.put(schema, key);
        }
        return key;
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
}
