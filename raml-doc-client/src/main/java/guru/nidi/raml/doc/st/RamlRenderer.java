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
import org.stringtemplate.v4.AttributeRenderer;

import java.util.Locale;

/**
 *
 */
class RamlRenderer implements AttributeRenderer {
    @Override
    public String toString(Object o, String formatString, Locale locale) {
        final Raml raml = (Raml) o;
        switch (formatString) {
            case "baseUri":
                if (raml.getProtocols() == null || raml.getProtocols().isEmpty()) {
                    return raml.getBaseUri();
                }
                final int pos = raml.getBaseUri().indexOf("://");
                final String rest = pos < 0 ? raml.getBaseUri() : raml.getBaseUri().substring(pos + 3);
                if (raml.getProtocols().size() == 2) {
                    return "http(s)://" + rest;
                }
                return raml.getProtocols().get(0).toString().toLowerCase() + "://" + rest;
            default:
                throw new IllegalArgumentException("unknown format '" + formatString + "'");
        }
    }
}
