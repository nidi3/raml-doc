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

import guru.nidi.loader.Loader;
import guru.nidi.raml.doc.GeneratorConfig;
import org.raml.model.Action;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.model.SecurityScheme;
import org.raml.model.parameter.AbstractParam;
import org.stringtemplate.v4.NoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupDir;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class Generator {
    private final GeneratorConfig config;

    public Generator(GeneratorConfig config) {
        this.config = config;
    }

    public File getTarget(Raml raml) {
        return new File(config.getTarget(), raml.getTitle());
    }

    public void generate(Raml raml) throws IOException {
        generate(raml, Collections.singletonList(raml));
    }

    public void generate(Raml raml, List<Raml> ramls) throws IOException {
        final STGroupDir group = initSTGroup(raml);

        if (raml == ramls.get(0)) {
            generateBase(raml, group);
        }

        final ST main = group.getInstanceOf("main/main");
        main.add("ramls", ramls);

        final String realBaseUri = config.getBaseUri() != null ? config.getBaseUri() : raml.getBaseUri();
        main.add("baseUri", config.hasFeature(Feature.TRYOUT) ? realBaseUri : null);
        main.add("download", config.hasFeature(Feature.DOWNLOAD));


        final File target = getTarget(raml);
        target.mkdirs();
        set(main, "raml", raml);
        render(main, "/main/doc", ".", new File(target, "index.html"));

        for (Resource resource : new RamlAdaptor().getAllResources(raml)) {
            set(main, "param", resource);
            final File file = new File(target, "resource/" + resource.getUri() + ".html");
            render(main, "/resource/resource", depth(resource.getUri()), file);
        }

        for (Map<String, SecurityScheme> sss : raml.getSecuritySchemes()) {
            for (Map.Entry<String, SecurityScheme> entry : sss.entrySet()) {
                set(main, "param", entry);
                final File file = new File(target, "security-scheme/" + entry.getKey() + ".html");
                render(main, "/securityScheme/securityScheme", "..", file);
            }
        }
    }

    private STGroupDir initSTGroup(Raml raml){
        final STGroupDir group = loadGroupDir("guru/nidi/raml/doc/st");

        group.registerModelAdaptor(Map.class, new EntrySetMapModelAdaptor());
        group.registerModelAdaptor(Raml.class, new RamlAdaptor());
        group.registerModelAdaptor(Resource.class, new ResourceAdaptor());
        group.registerModelAdaptor(Action.class, new ActionAdaptor(raml));

        group.registerRenderer(String.class, new StringRenderer(raml));
        group.registerRenderer(Boolean.class, new BooleanRenderer());
        group.registerRenderer(AbstractParam.class, new ParamRenderer());
        group.registerRenderer(Raml.class, new RamlRenderer());

        return group;
    }

    private void generateBase(Raml raml, STGroupDir group) throws IOException {
        config.getTarget().mkdirs();
        copyStaticResource(config.getTarget(), "favicon.ico", "ajax-loader.gif", "style.css",
                "script.js", "run_prettify.js", "beautify.js", "prettify-default.css");
        copyCustomResource(config.getTarget(), "favicon.ico");

        final ST index = group.getInstanceOf("main/index");
        index.add("firstIndex", raml.getTitle() + "/index.html");
        render(index, new File(config.getTarget(), "index.html"));
    }

    private void render(ST template, String sub, String relPath, File target) throws IOException {
        set(template, "template", sub);
        set(template, "relPath", relPath);
        render(template, target);
    }

    private void render(ST template, File file) throws IOException {
        file.getParentFile().mkdirs();
        try (final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), "utf-8")) {
            final StringWriter sw = new StringWriter();
            template.write(new NoIndentWriter(sw));
            out.write(sw.toString());
        }
    }

    private STGroupDir loadGroupDir(String path) {
        try {
            return new STGroupDir(path, '$', '$');
        } catch (IllegalArgumentException e) {
            //websphere classloader needs a trailing / to find directories on the classpath
            //-> add it to check if it exists, and then remove it
            final STGroupDir stGroupDir = new STGroupDir(path + "/", '$', '$');
            final String url = stGroupDir.root.toExternalForm();
            try {
                stGroupDir.root = new URL(url.substring(0, url.length() - 1));
                return stGroupDir;
            } catch (MalformedURLException me) {
                throw new IllegalArgumentException(me);
            }
        }
    }

    private void copyStaticResource(File base, String... names) throws IOException {
        for (String name : names) {
            try (final InputStream in = getClass().getResourceAsStream("/guru/nidi/raml/doc/static/" + name);
                 final FileOutputStream out = new FileOutputStream(new File(base, name))) {
                copy(in, out);
            }
        }
    }

    private void copyCustomResource(File base, String... names) throws IOException {
        for (String name : names) {
            try (final InputStream in = config.loadCustomization(name);
                 final FileOutputStream out = new FileOutputStream(new File(base, name))) {
                copy(in, out);
            } catch (Loader.ResourceNotFoundException e) {
                //ignore
            }
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

    private void copy(InputStream in, OutputStream out) throws IOException {
        final byte[] buf = new byte[1000];
        int read;
        while ((read = in.read(buf)) > 0) {
            out.write(buf, 0, read);
        }
    }
}
