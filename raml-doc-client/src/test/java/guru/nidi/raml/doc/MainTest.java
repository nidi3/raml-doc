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

import java.io.File;
import java.net.URI;

/**
 *
 */
public class MainTest {
    @Test
    public void basic() throws Exception {
        final SchemaTree tree = new SchemaLoader().get(URI.create("file:///" + new File("src/test/resources/schema.json").getAbsolutePath()));
        new Generator().generate(new File("src/test/resources/basic.raml"), new File("target"));
    }
}
