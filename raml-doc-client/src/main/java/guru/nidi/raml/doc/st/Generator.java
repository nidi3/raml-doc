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
import org.raml.model.Resource;
import org.raml.model.SecurityScheme;
import org.raml.model.parameter.AbstractParam;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupDir;

import java.io.*;
import java.util.Map;

/**
 *
 */
public class Generator {
    public File generate(String ramlLocation, File target) throws IOException {
        final Raml raml = loadRaml(ramlLocation);
        final STGroupDir group = new STGroupDir("st", '$', '$');
        group.registerModelAdaptor(Map.class, new EntrySetMapModelAdaptor());
        group.registerRenderer(String.class, new StringRenderer(raml));
        group.registerRenderer(Boolean.class, new BooleanRenderer());
        group.registerRenderer(AbstractParam.class, new ParamRenderer());
        group.registerRenderer(Raml.class, new RamlRenderer());
        group.registerModelAdaptor(Action.class, new ActionAdaptor(raml));
        final ST main = group.getInstanceOf("main/main");
        main.add("raml", raml);
        final Util util = new Util(raml);
        main.add("util", util);

        final File base = new File(target, raml.getTitle());
        base.mkdirs();
        main.add("template", "/main/doc");
        main.add("relPath", ".");
        render(main, new File(base, "index.html"));

        set(main, "template", "/resource/resource");
        for (Resource resource : util.getAllResources()) {
            set(main, "param", resource);
            set(main, "relPath", depth(resource.getUri()));
            final File file = new File(base, "resource/" + resource.getUri() + ".html");
            render(main, file);
        }

        set(main, "template", "/securityScheme/securityScheme");
        for (Map<String, SecurityScheme> sss : raml.getSecuritySchemes()) {
            for (Map.Entry<String, SecurityScheme> entry : sss.entrySet()) {
                set(main, "param", entry.getValue());
                set(main, "relPath", "../.");
                final File file = new File(base, "security-scheme/" + entry.getKey() + ".html");
                render(main, file);
            }
        }
        try (final InputStream in = getClass().getResourceAsStream("/style.css");
             final FileOutputStream out = new FileOutputStream(new File(base, "style.css"))) {
            copy(in, out);
        }

        return new File(target, raml.getTitle());
    }

    private Raml loadRaml(String ramlLocation) throws IOException {
        try {
            return new RamlDocumentBuilder().build(ramlLocation);
        } catch (Exception e) {
            throw new IOException("No raml found at location '" + ramlLocation + "'");
        }
    }

    private void set(ST template, String name, Object value) {
        template.remove(name);
        template.add(name, value);
    }

    private String depth(String s) {
        String res = "";
        int pos = -1;
        while ((pos = s.indexOf('/', pos + 1)) >= 0) {
            res += "../";
        }
        return res + ".";
    }

    private void render(ST template, File file) throws IOException {
        file.getParentFile().mkdirs();
        try (final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file))) {
            out.write(template.render());
        }
    }

    private void copy(InputStream in, OutputStream out) throws IOException {
        final byte[] buf = new byte[1000];
        int read;
        while ((read = in.read(buf)) > 0) {
            out.write(buf, 0, read);
        }
    }

}
