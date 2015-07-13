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

import org.raml.model.parameter.AbstractParam;
import org.stringtemplate.v4.AttributeRenderer;

import java.util.Locale;

/**
 *
 */
class ParamRenderer implements AttributeRenderer {
    @Override
    public String toString(Object o, String formatString, Locale locale) {
        final AbstractParam param = (AbstractParam) o;
        String s = "";
        switch (formatString) {
            case "intLimit":
                if (param.getMinimum() != null) {
                    s += param.getMinimum() + "<=";
                }
                if (param.getMinimum() != null || param.getMaximum() != null) {
                    s += "x";
                }
                if (param.getMaximum() != null) {
                    s += "<=" + param.getMaximum();
                }
                return s;
            case "strLimit":
                if (param.getMinLength() != null) {
                    s += param.getMinLength() + "<=";
                }
                if (param.getMinLength() != null || param.getMaxLength() != null) {
                    s += "length";
                }
                if (param.getMaxLength() != null) {
                    s += "<=" + param.getMaxLength();
                }
                return s;
            default:
                throw new IllegalArgumentException("unknown format '" + formatString + "'");
        }
    }
}
