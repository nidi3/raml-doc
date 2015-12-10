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

import guru.nidi.raml.doc.st.Feature;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.EnumSet;

/**
 *
 */
public class GeneratorTest {
    @Test
    public void basic() throws Exception {
        new GeneratorConfig(Arrays.asList("file://src/test/resources/data/basic.raml"), new File("target/basicTryOut"),
                EnumSet.allOf(Feature.class), "http://localhost:8080", null, null, false)
                .generate();
        new GeneratorConfig(Arrays.asList("file://src/test/resources/data/basic.raml"), new File("target/basic"),
                EnumSet.noneOf(Feature.class), "http://localhost:8080", null, null, false)
                .generate();
    }

    @Test
    public void beautify() throws ScriptException {
        final ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");
        final Object eval2 = engine.eval("var window=this;");
        final Object eval = engine.eval(new InputStreamReader(getClass().getResourceAsStream("/guru/nidi/raml/doc/static/beautify.js")));
        String s = "if (\"this_is\" == /an_example/) {of_beautifer();} else {var a = b ? (c % d) : e[f];}";
        final Object eval1 = engine.eval("print(js_beautify('" + s + "'));");
    }
}
