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
package guru.nidi.raml.doc.servlet;

import guru.nidi.loader.Loader;
import guru.nidi.loader.basic.UriLoader;
import guru.nidi.raml.doc.GeneratorConfig;
import guru.nidi.raml.doc.st.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class RamlDocServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(RamlDocServlet.class);
    private static final Map<String, String> mimeTypes = new HashMap<String, String>() {{
        put("html", "text/html");
        put("css", "text/css");
        put("js", "text/javascript");
    }};

    private enum InitParameter {
        LOCATIONS("ramlLocations"),
        FEATURES("features"),
        BASE_URI("baseUri"),
        BASE_URI_PARAMS("baseUriParameters"),
        CUSTOMIZATION("customization");

        private final String value;

        InitParameter(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static InitParameter byValue(String value) {
            for (final InitParameter ip : values()) {
                if (ip.getValue().equals(value)) {
                    return ip;
                }
            }
            return null;
        }
    }

    private Initer initer;

    @Override
    public void init() throws ServletException {
        final List<String> unknown = unknownParameters();
        if (!unknown.isEmpty()) {
            log.warn("Unknown init-parameters: " + unknown);
        }
        initer = new Initer();
    }

    @Override
    public void destroy() {
        initer.destroy();
    }

    protected List<String> unknownParameters() {
        final List<String> res = new ArrayList<>();
        final Enumeration<String> names = getInitParameterNames();
        while (names.hasMoreElements()) {
            final String name = names.nextElement();
            if (InitParameter.byValue(name) == null) {
                res.add(name);
            }
        }
        return res;
    }

    private String initParameter(InitParameter ip) {
        return getInitParameter(ip.getValue());
    }

    protected String ramlLocations() {
        return initParameter(InitParameter.LOCATIONS);
    }

    protected EnumSet<Feature> features() {
        return Feature.parse(initParameter(InitParameter.FEATURES));
    }

    protected String baseUri() {
        return initParameter(InitParameter.BASE_URI);
    }

    protected String baseUriParameters() {
        return initParameter(InitParameter.BASE_URI_PARAMS);
    }

    protected String customization() {
        return initParameter(InitParameter.CUSTOMIZATION);
    }

    protected Loader getCustomization() {
        final String base = customization() == null ? "classpath://guru/nidi/raml/doc/custom" : customization();
        return new UriLoader() {
            @Override
            public InputStream fetchResource(String name, long ifModifiedSince) {
                return super.fetchResource(base + "/" + name, ifModifiedSince);
            }
        };
    }

    protected GeneratorConfig createGeneratorConfig() {
        return new GeneratorConfig(getRamlLocations(), docDir(), features(), baseUri(), baseUriParameters(), getCustomization(), true);
    }

    protected String getRamlLocations() {
        final String locations = ramlLocations();
        return locations == null ? "classpath://api.raml" : locations;
    }

    private File docDir() {
        final File tempDir = new File(System.getProperty("java.io.tmpdir"));
        return new File(tempDir, "raml-doc/" + getServletName());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (!initer.waitReady()) {
            super.doGet(req, res);
            return;
        }
        if (req.getPathInfo() == null || req.getPathInfo().length() <= 1) {
            res.sendRedirect(req.getRequestURL().append("/" + initer.baseDir + "/index.html").toString().replaceAll("([^:])/+", "$1/"));
            return;
        }
        final File source = new File(docDir(), req.getPathInfo());
        if (!source.exists() || !source.isFile()) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            setContentType(res, source.getName());
            try (final InputStream in = new FileInputStream(source);
                 final OutputStream out = new BufferedOutputStream(res.getOutputStream())) {
                copy(in, out);
            }
        }
        res.flushBuffer();
    }

    private void setContentType(HttpServletResponse res, String source) {
        final int pos = source.lastIndexOf('.');
        if (pos < source.length()) {
            final String suffix = source.substring(pos + 1);
            final String mimeType = mimeTypes.get(suffix);
            if (mimeType != null) {
                res.setHeader("Content-Type", mimeType);
            }
        }
    }

    private void copy(InputStream in, OutputStream out) throws IOException {
        final byte[] buf = new byte[10000];
        int read;
        while ((read = in.read(buf)) > 0) {
            out.write(buf, 0, read);
        }
    }

    private class Initer {
        private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        private String baseDir;

        public Initer() {
            executor.schedule(new Runnable() {
                @Override
                public void run() {
                    try {
                        baseDir = createGeneratorConfig().generate();
                    } catch (Exception e) {
                        log.error("Could not create RAML documentation", e);
                    }
                }
            }, 10, TimeUnit.SECONDS);
            executor.shutdown();
        }

        public boolean waitReady() {
            if (!features().contains(Feature.ONLINE)) {
                return false;
            }
            if (!executor.isTerminated()) {
                try {
                    executor.awaitTermination(1, TimeUnit.MINUTES);
                    executor.shutdownNow();
                } catch (InterruptedException e) {
                    //ignore
                }
            }
            return true;
        }

        public void destroy() {
            executor.shutdownNow();
        }
    }
}
