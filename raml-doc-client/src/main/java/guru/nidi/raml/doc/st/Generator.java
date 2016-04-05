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

import biz.gabrys.lesscss.compiler.CompilerException;
import biz.gabrys.lesscss.compiler.LessCompilerImpl;
import guru.nidi.loader.Loader;
import guru.nidi.raml.doc.GeneratorConfig;
import guru.nidi.raml.doc.HtmlOptimizer;
import guru.nidi.raml.doc.IoUtil;
import org.raml.model.Action;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.model.SecurityScheme;
import org.raml.model.parameter.AbstractParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.NoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupDir;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 *
 */
public class Generator {
    private static final Logger log = LoggerFactory.getLogger(Generator.class);

    private static final List<String> STATIC_FILES = loadStaticFileList();

    private static List<String> loadStaticFileList() {
        final List<String> res = new ArrayList<>();
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(Generator.class.getResourceAsStream("/guru/nidi/raml/doc/static-files.lst")))) {
            String line;
            while ((line = in.readLine()) != null) {
                res.add(line);
            }
        } catch (IOException e) {
            throw new AssertionError("Could not load static file list: " + e.getMessage());
        }
        return res;
    }

    private static final List<String> CUSTOM_FILES = Arrays.asList(
            "favicon.ico", "custom-variables.less", "custom-style.less");

    private final GeneratorConfig config;

    public Generator(GeneratorConfig config) {
        this.config = config;
    }

    public File getTarget(Raml raml) {
        return new File(config.getTarget(), filenameFor(raml));
    }

    private String filenameFor(Raml raml) {
        return GeneratorConfig.safeName(raml);
    }

    public void generate(Raml raml) throws IOException {
        generate(Collections.singletonList(raml));
    }

    public void generate(List<Raml> ramls) throws IOException {
        for (final Raml raml : ramls) {
            doGenerate(raml, ramls);
        }
    }

    private void doGenerate(Raml raml, List<Raml> ramls) throws IOException {
        final STGroupDir group = initSTGroup(raml);

        if (raml == ramls.get(0)) {
            config.getTarget().mkdirs();
            if (!config.isForceDelete()) {
                checkTargetEmpty(config.getTarget(), ramls);
            }
            deleteAll(config.getTarget());
            generateBase(raml, group);
        }

        final File target = getTarget(raml);
        target.mkdirs();

        final ST main = group.getInstanceOf("main/main");
        main.add("ramls", ramls);
        main.add("baseUri", config.hasFeature(Feature.TRYOUT) ? config.getBaseUri(raml) : null);
        main.add("download", config.hasFeature(Feature.DOWNLOAD));
        main.add("docson", config.hasFeature(Feature.DOCSON));

        set(main, "raml", raml);
        render(main, "/main/doc", ".", new File(target, "index.html"));

        renderResources(raml, main, target);
        renderSecurity(raml, main, target);
    }

    private void renderSecurity(Raml raml, ST main, File target) throws IOException {
        for (Map<String, SecurityScheme> sss : raml.getSecuritySchemes()) {
            for (Map.Entry<String, SecurityScheme> entry : sss.entrySet()) {
                set(main, "param", entry);
                final File file = new File(target, "security-scheme/" + entry.getKey() + ".html");
                render(main, "/securityScheme/securityScheme", "..", file);
            }
        }
    }

    private void renderResources(Raml raml, ST main, File target) throws IOException {
        for (Resource resource : new RamlAdaptor().getAllResources(raml)) {
            set(main, "param", resource);
            final File file = new File(target, "resource" + IoUtil.safePath(resource.getUri()) + ".html");
            render(main, "/resource/resource", depth(resource.getUri()), file);
        }
    }

    private void checkTargetEmpty(File target, List<Raml> ramls) {
        for (final File file : target.listFiles()) {
            if (!isAllowedInTarget(file.getName(), ramls)) {
                throw new IllegalStateException("Cannot generate doc in folder '" + target + "' because it is not empty. " +
                        "Contains " + (file.isDirectory() ? "directory" : "file") + " '" + file.getName() + "'.");
            }
        }
    }

    private boolean isAllowedInTarget(String name, List<Raml> ramls) {
        return "index.html".equals(name) || existsTitle(name, ramls) ||
                STATIC_FILES.contains(name) || (name.endsWith(".css") && STATIC_FILES.contains(name.substring(0, name.length() - 4) + ".less"));
    }

    private boolean existsTitle(String name, List<Raml> ramls) {
        for (final Raml raml : ramls) {
            if (name.equals(filenameFor(raml))) {
                return true;
            }
        }
        return false;
    }

    private void deleteAll(File target) {
        for (final File file : target.listFiles()) {
            if (file.isDirectory()) {
                deleteAll(file);
            }
            file.delete();
        }
    }

    private STGroupDir initSTGroup(Raml raml) {
        final STGroupDir group = loadGroupDir("guru/nidi/raml/doc/st-templates");

        group.registerModelAdaptor(Map.class, new EntrySetMapModelAdaptor());
        group.registerModelAdaptor(Raml.class, new RamlAdaptor());
        group.registerModelAdaptor(Resource.class, new ResourceAdaptor());
        group.registerModelAdaptor(Action.class, new ActionAdaptor(raml));

        group.registerRenderer(String.class, new StringRenderer(raml, config.getSchemaCache()));
        group.registerRenderer(AbstractParam.class, new ParamRenderer());
        group.registerRenderer(Raml.class, new RamlRenderer());

        return group;
    }

    private void generateBase(Raml raml, STGroupDir group) throws IOException {
        copyStaticResources(config.getTarget(), STATIC_FILES);
        copyCustomResources(config.getTarget(), CUSTOM_FILES);
        transformLessResources(config.getTarget());

        final ST index = group.getInstanceOf("main/index");
        index.add("firstIndex", filenameFor(raml) + "/index.html");
        render(index, new File(config.getTarget(), "index.html"));
    }

    private void render(ST template, String sub, String relPath, File target) throws IOException {
        set(template, "template", sub);
        set(template, "relPath", relPath);
        render(template, target);
        new HtmlOptimizer().optimizeColumnWidths(target);
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

    private void copyStaticResources(File base, List<String> names) throws IOException {
        for (String name : names) {
            final File file = new File(base, name);
            file.getParentFile().mkdirs();
            try (final InputStream in = getClass().getResourceAsStream("/guru/nidi/raml/doc/static/" + name);
                 final FileOutputStream out = new FileOutputStream(file)) {
                copy(in, out);
            }
        }
    }

    private void copyCustomResources(File base, List<String> names) throws IOException {
        for (String name : names) {
            try (final InputStream in = config.loadCustomization(name);
                 final FileOutputStream out = new FileOutputStream(new File(base, name))) {
                copy(in, out);
                log.info("Using custom " + name);
            } catch (Loader.ResourceNotFoundException e) {
                //ignore
            }
        }
    }

    private void transformLessResources(File base) throws IOException {
        final LessCompilerImpl compiler = new LessCompilerImpl();
        for (File file : base.listFiles()) {
            final String name = file.getName();
            try {
                if (name.endsWith(".less")) {
                    try (final Writer out = new OutputStreamWriter(new FileOutputStream(new File(file.getParentFile(), name.substring(0, name.length() - 5) + ".css")), "utf-8")) {
                        out.write(compiler.compile(file));
                    }
                }
            } catch (CompilerException e) {
                log.error("Problem compiling '" + name + "'\n" + stackTraceWithoutCause(e));
            }
        }
    }

    private String stackTraceWithoutCause(Exception e) {
        String s = e.toString() + "\n";
        for (StackTraceElement traceElement : e.getStackTrace()) {
            s += "\tat " + traceElement + "\n";
        }
        return s;
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
