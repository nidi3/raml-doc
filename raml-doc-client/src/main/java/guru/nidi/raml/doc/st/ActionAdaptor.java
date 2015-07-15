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

import org.raml.model.Action;
import org.raml.model.Raml;
import org.raml.model.SecurityReference;
import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ModelAdaptor;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.ObjectModelAdaptor;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

import java.util.List;

/**
 *
 */
class ActionAdaptor extends ObjectModelAdaptor {
    private final Raml raml;

    ActionAdaptor(Raml raml) {
        this.raml = raml;
    }

//    @Override
//    public String toString(Object o, String formatString, Locale locale) {
//        Action a = (Action) o;
//        if (formatString == null) {
//            return a.toString();
//        }
//        switch (formatString) {
//            case "securedBy":
//                if (a.getSecuredBy() != null && !a.getSecuredBy().isEmpty()) {
//                    return toString(a.getSecuredBy());
//                }
//                if (a.getResource().getSecuredBy() != null && !a.getResource().getSecuredBy().isEmpty()) {
//                    return toString(a.getResource().getSecuredBy());
//                }
//                if (raml.getSecuredBy() != null && !raml.getSecuredBy().isEmpty()) {
//                    return toString(raml.getSecuredBy());
//                }
//                return "[]";
//            default:
//                throw new IllegalArgumentException("unknown format '" + formatString + "'");
//        }
//    }

    private String toString(List<SecurityReference> refs) {
        String s = "[";
        for (SecurityReference ref : refs) {
            s += ref.getName() + ",";
        }
        return s.substring(0, s.length() - 1) + "]";
    }

    @Override
    public Object getProperty(Interpreter interp, ST self, Object o, Object property, String propertyName) throws STNoSuchPropertyException {
        if ("securitySchemes".equals(propertyName)) {
            final Action a = (Action) o;
            if (a.getSecuredBy() != null && !a.getSecuredBy().isEmpty()) {
                return a.getSecuredBy();
            }
            if (a.getResource().getSecuredBy() != null && !a.getResource().getSecuredBy().isEmpty()) {
                return a.getResource().getSecuredBy();
            }
            if (raml.getSecuredBy() != null && !raml.getSecuredBy().isEmpty()) {
                return raml.getSecuredBy();
            }
        }
        return super.getProperty(interp, self, o, property, propertyName);
    }
}