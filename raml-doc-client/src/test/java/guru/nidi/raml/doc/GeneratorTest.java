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

import com.github.fge.jsonschema.core.load.SchemaLoader;
import com.github.fge.jsonschema.core.tree.SchemaTree;
import guru.nidi.raml.doc.st.Generator;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;

/**
 *
 */
public class GeneratorTest {
    @Test
    public void basic() throws Exception {
        final SchemaTree tree = new SchemaLoader().get(URI.create("file:///" + new File("src/test/resources/schema.json").getAbsolutePath()));
        new Generator().tryOut(true).generate("file:src/test/resources/basic.raml", new File("target/tryout"));
        new Generator().tryOut(false).generate("file:src/test/resources/basic.raml", new File("target"));
        new Generator().tryOut(false).generate("file:src/test/resources/GaiaNewListServices.raml", new File("target"));
    }

    @Test
    public void beautify() throws ScriptException {
        final ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");
        final Object eval2 = engine.eval("var window=this;");
        final Object eval = engine.eval(new InputStreamReader(getClass().getResourceAsStream("/beautify.js")));
        String s = "if (\"this_is\" == /an_example/) {of_beautifer();} else {var a = b ? (c % d) : e[f];}";
        final Object eval1 = engine.eval("print(js_beautify('" + s + "'));");
    }
}
