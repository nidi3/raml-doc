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

import guru.nidi.raml.doc.IoUtil;
import org.raml.model.Raml;
import org.stringtemplate.v4.AttributeRenderer;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Locale;


/**
 *
 */
class StringRenderer implements AttributeRenderer {
    private final Raml raml;
    private final JsBeautifyer jsBeautifyer;
    private final MarkdownProcessor markdownProcessor;

    public StringRenderer(Raml raml) {
        this.raml = raml;
        final ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");
        final Invocable invocable = (Invocable) engine;
        try {
            engine.eval("var window=this;");
            //beautify.js: changed default operators like 'bla || 0' into 'bla | 0' (jdk 6)
            engine.eval(new InputStreamReader(getClass().getResourceAsStream("/guru/nidi/raml/doc/static/beautify.js"), "utf-8"));
            engine.eval("jsBeautify=js_beautify;");
            jsBeautifyer = invocable.getInterface(JsBeautifyer.class);

            //marked.js: commented lines 723,727 because of unrecognized unicode chars (jdk 6)
            engine.eval("Inline=null;");
            engine.eval(new InputStreamReader(getClass().getResourceAsStream("/guru/nidi/raml/doc/marked.js"), "utf-8"));
            markdownProcessor = invocable.getInterface(MarkdownProcessor.class);
        } catch (ScriptException | UnsupportedEncodingException e) {
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
            case "nameUrl":
                return IoUtil.urlEncodedSafeName(s);
            case "pathUrl":
                return IoUtil.urlEncodedSafePath(s);
            default:
                throw new IllegalArgumentException("unknown format '" + formatString + "'");
        }
    }

    private String schema(String s) {
        final String refSchema = raml.getConsolidatedSchemas().get(s);
        return refSchema == null ? s : refSchema;
    }

    private String js(String s) {
        return jsBeautifyer.jsBeautify(s).replace("<", "&lt;");
    }

    private String summary(String s) {
        final int pos = s.indexOf('.');
        return pos < 0 ? s : s.substring(0, pos + 1);
    }

    private String markdown(String s) {
        return markdownProcessor.marked(s);
    }

    public interface JsBeautifyer {
        String jsBeautify(String s);
    }

    public interface MarkdownProcessor {
        String marked(String s);
    }
}
