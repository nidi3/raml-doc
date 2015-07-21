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

import org.pegdown.PegDownProcessor;
import org.raml.model.Raml;
import org.stringtemplate.v4.AttributeRenderer;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.InputStreamReader;
import java.util.Locale;

/**
 *
 */
class StringRenderer implements AttributeRenderer {
    private final PegDownProcessor processor;
    private final Raml raml;
    private final ScriptEngine engine;
    private final Invocable invocable;

    public StringRenderer(Raml raml) {
        this.raml = raml;
        processor = new PegDownProcessor();
        engine = new ScriptEngineManager().getEngineByExtension("js");
        invocable = (Invocable) engine;
        try {
            engine.eval("var window=this;");
            engine.eval(new InputStreamReader(getClass().getResourceAsStream("/beautify.js")));
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString(Object o, String formatString, Locale locale) {
        String s = (String) o;
        if (formatString == null) {
            return s;
        }
        for (String fmt : formatString.split(",")) {
            s = format(s, fmt);
        }
        return s;
    }

    private String format(String s, String formatString) {
        switch (formatString) {
            case "markdown":
                return markdown(s);
            case "summary":
                return summary(s);
            case "schema":
                return schema(s);
            case "js":
                return js(s);
            default:
                throw new IllegalArgumentException("unknown format '" + formatString + "'");
        }
    }

    private String schema(String s) {
        final String refSchema = raml.getConsolidatedSchemas().get(s);
        return refSchema == null ? s : refSchema;
    }

    private String js(String s) {
        try {
            return ((String) invocable.invokeFunction("js_beautify", s)).replace("<","&lt;");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String summary(String s) {
        final int pos = s.indexOf('.');
        return pos < 0 ? s : s.substring(0, pos + 1);
    }

    private String markdown(String s) {
        return processor.markdownToHtml(s);
    }
}
