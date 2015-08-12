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
import org.stringtemplate.v4.NoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupDir;
import org.stringtemplate.v4.compiler.CompiledST;

import java.io.*;
import java.util.*;

/**
 *
 */
public class Generator {
    private final File target;
    private EnumSet<Feature> features = EnumSet.noneOf(Feature.class);
    private String baseUri;
    private String parentTitle;

    public Generator(File target) {
        this.target = target;
    }

    public Generator features(EnumSet<Feature> features) {
        this.features = features;
        return this;
    }

    public Generator features(Feature... features) {
        return features(EnumSet.copyOf(Arrays.asList(features)));
    }

    public Generator baseUri(String baseUri) {
        this.baseUri = baseUri;
        return this;
    }

    public Generator parentTitle(String parentTitle) {
        this.parentTitle = parentTitle;
        return this;
    }

    public File getTarget(Raml raml) {
        return new File(target, raml.getTitle());
    }

    public void generate(Raml raml) throws IOException {
        generate(raml, Collections.singletonList(raml));
    }

    public void generate(Raml raml, List<Raml> ramls) throws IOException {
        final STGroupDir group = loadGroupDir("guru/nidi/raml/doc/st");
        group.registerModelAdaptor(Map.class, new EntrySetMapModelAdaptor());
        group.registerModelAdaptor(Raml.class, new RamlAdaptor());
        group.registerModelAdaptor(Resource.class, new ResourceAdaptor());
        group.registerModelAdaptor(Action.class, new ActionAdaptor(raml));

        group.registerRenderer(String.class, new StringRenderer(raml));
        group.registerRenderer(Boolean.class, new BooleanRenderer());
        group.registerRenderer(AbstractParam.class, new ParamRenderer());
        group.registerRenderer(Raml.class, new RamlRenderer());
        final ST main = group.getInstanceOf("main/main");
        main.add("ramls", ramls);

        final String realBaseUri = baseUri != null ? baseUri : raml.getBaseUri();
        main.add("baseUri", features.contains(Feature.TRYOUT) ? realBaseUri : null);
        main.add("download", features.contains(Feature.DOWNLOAD));

        target.mkdirs();
        copyResource(target, "favicon.ico", "ajax-loader.gif", "style.css",
                "script.js", "run_prettify.js", "beautify.js", "prettify-default.css");

        final Raml parent = new Raml();
        parent.setTitle(parentTitle == null ? ramls.get(0).getTitle() : parentTitle);
        main.add("raml", parent);
        render(main, "/main/docMain", ".", ".", new File(target, "index.html"));

        final File target = getTarget(raml);
        target.mkdirs();
        set(main, "raml", raml);
        render(main, "/main/doc", "..", ".", new File(target, "index.html"));

        for (Resource resource : new RamlAdaptor().getAllResources(raml)) {
            set(main, "param", resource);
            final File file = new File(target, "resource/" + resource.getUri() + ".html");
            render(main, "/resource/resource", "../" + depth(resource.getUri()), depth(resource.getUri()), file);
        }

        for (Map<String, SecurityScheme> sss : raml.getSecuritySchemes()) {
            for (Map.Entry<String, SecurityScheme> entry : sss.entrySet()) {
                set(main, "param", entry);
                final File file = new File(target, "security-scheme/" + entry.getKey() + ".html");
                render(main, "/securityScheme/securityScheme", "../..", "..", file);
            }
        }
    }

    private void render(ST template, String sub, String basePath, String relPath, File target) throws IOException {
        set(template, "template", sub);
        set(template, "basePath", basePath);
        set(template, "relPath", relPath);
        render(template, target);
    }

    private STGroupDir loadGroupDir(String path) {
        try {
            return new STGroupDir(path, '$', '$');
        } catch (IllegalArgumentException e) {
            return new TrailingSlashSTGroupDir(path, '$', '$');
        }
    }

    private void copyResource(File base, String... names) throws IOException {
        for (String name : names) {
            try (final InputStream in = getClass().getResourceAsStream("/guru/nidi/raml/doc/static/" + name);
                 final FileOutputStream out = new FileOutputStream(new File(base, name))) {
                copy(in, out);
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
        return res+".";
    }

    private void render(ST template, File file) throws IOException {
        file.getParentFile().mkdirs();
        try (final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), "utf-8")) {
            final StringWriter sw = new StringWriter();
            template.write(new NoIndentWriter(sw));
            out.write(sw.toString());
        }
    }

    private void copy(InputStream in, OutputStream out) throws IOException {
        final byte[] buf = new byte[1000];
        int read;
        while ((read = in.read(buf)) > 0) {
            out.write(buf, 0, read);
        }
    }

    //websphere classloader needs a trailing / to find directories on the classpath
    //-> add it, and remove it to load files
    private static class TrailingSlashSTGroupDir extends STGroupDir {
        public TrailingSlashSTGroupDir(String dirName, char delimiterStartChar, char delimiterStopChar) {
            super(dirName + "/", delimiterStartChar, delimiterStopChar);
        }

        @Override
        protected CompiledST load(String name) {
            return super.load(name.substring(1));
        }
    }
}
