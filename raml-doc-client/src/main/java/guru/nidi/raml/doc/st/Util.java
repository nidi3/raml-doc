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
package guru.nidi.raml.doc.st;

import org.raml.model.Raml;
import org.raml.model.Resource;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 */
class Util {
    private final Raml raml;

    public Util(Raml raml) {
        this.raml = raml;
    }

    public Map<String, Resource> getAllResources() {
        final Map<String, Resource> res = new LinkedHashMap<>();
        for (Map.Entry<String, Resource> entry : raml.getResources().entrySet()) {
            res.put(entry.getKey(), entry.getValue());
            addSubResources(entry.getKey(), entry.getValue(), res);
        }
        return res;
    }

    private void addSubResources(String path, Resource resource, Map<String, Resource> res) {
        for (Map.Entry<String, Resource> entry : resource.getResources().entrySet()) {
            res.put(path + entry.getKey(), entry.getValue());
            addSubResources(path + entry.getKey(), entry.getValue(), res);
        }
    }


}
