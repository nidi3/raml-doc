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

import org.raml.model.Resource;
import org.raml.model.parameter.UriParameter;
import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.ObjectModelAdaptor;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
class ResourceAdaptor extends ObjectModelAdaptor {
    @Override
    public Object getProperty(Interpreter interp, ST self, Object o, Object property, String propertyName) throws STNoSuchPropertyException {
        final Resource res = (Resource) o;
        switch (propertyName) {
            case "resolvedUriParameters":
                final Map<String, UriParameter> params = new HashMap<>();
                getAllResources(res, params);
                return params;
            default:
                return super.getProperty(interp, self, o, property, propertyName);
        }
    }

    public void getAllResources(Resource resource, Map<String, UriParameter> res) {
        res.putAll(resource.getUriParameters());
        if (resource.getParentResource() != null) {
            getAllResources(resource.getParentResource(), res);
        }
    }

}
