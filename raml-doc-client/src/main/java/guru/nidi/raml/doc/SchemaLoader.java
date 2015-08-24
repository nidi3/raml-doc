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

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.nidi.loader.Loader;
import org.raml.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class SchemaLoader {
    private final Raml raml;
    private final String loc;
    private final Loader loader;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Set<String> loaded = new HashSet<>();

    public SchemaLoader(Raml raml, String loc, Loader loader) {
        this.raml = raml;
        this.loc = loc;
        this.loader = loader;
    }

    public void loadSchemas() {
        for (final Resource resource : raml.getResources().values()) {
            loadSchemas(resource);
        }
    }

    private void loadSchemas(Resource resource) {
        for (final Resource res : resource.getResources().values()) {
            loadSchemas(res);
        }
        for (final Action action : resource.getActions().values()) {
            loadSchemas(action);
        }
    }

    private void loadSchemas(Action action) {
        if (action.getBody() != null) {
            for (final MimeType mime : action.getBody().values()) {
                loadSchema(mime);
            }
        }
        for (final Response response : action.getResponses().values()) {
            loadSchemas(response);
        }
    }

    private void loadSchemas(Response response) {
        if (response.getBody() != null) {
            for (final MimeType mime : response.getBody().values()) {
                loadSchema(mime);
            }
        }
    }

    private void loadSchema(MimeType mime) {
        final String schema = mime.getSchema();
        if (schema == null) {
            return;
        }
        final String refSchema = raml.getConsolidatedSchemas().get(schema);
        final String schemaToUse = refSchema == null ? schema : refSchema;

        final String type = mime.getType();
        final int pos = type.indexOf(';');
        final String simpleType = pos < 0 ? type : type.substring(0, pos);
        if (simpleType.equals("application/json") || simpleType.endsWith("+json")) {
            try {
                loadJsonSchema(loc, schemaToUse);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void loadJsonSchema(String loc, String schema) throws IOException {
        final Map<String, Object> map = mapper.readValue(schema, Map.class);
        loadJsonSchema(map.get("id") == null ? loc : (String) map.get("id"), map);
    }

    private void loadJsonSchema(String loc, Map<String, Object> map) throws IOException {
        for (final Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                loadJsonSchema(loc, (Map<String, Object>) entry.getValue());
            } else if ("$ref".equals(entry.getKey())) {
                doLoadJsonSchema(resolveUri(loc, (String) entry.getValue()));
            }
        }
    }

    private String resolveUri(String base, String target) {
        final int hash = target.indexOf("#");
        final int len = target.length() > hash + 1 && target.charAt(hash + 1) == '/' ? 2 : 1;
        if (hash == 0) {
            target = target.substring(len);
        } else if (hash > 0) {
            target = target.substring(0, hash);
        }
        base = base.substring(0, base.lastIndexOf('/') + 1);
        return IoUtil.normalizePath(base + target);
    }

    private void doLoadJsonSchema(String file) throws IOException {
        if (!loaded.contains(file)) {
            try (final BufferedReader ref = new BufferedReader(new InputStreamReader(loader.fetchResource(file, -1), "utf-8"))) {
                final StringBuilder s = new StringBuilder();
                String line;
                while ((line = ref.readLine()) != null) {
                    s.append(line);
                }
                loadJsonSchema(file, s.toString());
            }
            loaded.add(file);
        }
    }
}
