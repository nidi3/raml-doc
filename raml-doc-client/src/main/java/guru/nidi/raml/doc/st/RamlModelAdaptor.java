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
import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.ObjectModelAdaptor;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
class RamlModelAdaptor extends ObjectModelAdaptor {
    @Override
    public Object getProperty(Interpreter interp, ST self, Object o, Object property, String propertyName) throws STNoSuchPropertyException {
        if ("allResources".equals(propertyName)) {
            return getAllResources((Raml) o);
        }
        return super.getProperty(interp, self, o, property, propertyName);
    }

    public List<Resource> getAllResources(Raml raml) {
        final List<Resource> res = new ArrayList<>();
        for (Resource r : raml.getResources().values()) {
            res.add(r);
            addSubResources(r, res);
        }
        return res;
    }

    private void addSubResources(Resource resource, List<Resource> res) {
        for (Resource r : resource.getResources().values()) {
            res.add(r);
            addSubResources(r, res);
        }
    }

}
