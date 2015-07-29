package guru.nidi.raml.doc;/*
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

import guru.nidi.raml.doc.st.Generator;
import guru.nidi.raml.loader.RamlLoaders;
import org.raml.model.Raml;

import java.io.File;

/**
 *
 */
public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java -jar raml-doc-standalone.jar <file>");
            System.exit(1);
        }
        try {
            final Raml raml = RamlLoaders.absolutely().load(args[0]);
            new Generator().generate(raml, new File("."));
        } catch (Exception e) {
            System.out.println("Problem generating doc");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
