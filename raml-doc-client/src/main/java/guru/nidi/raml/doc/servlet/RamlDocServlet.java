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
import java.util.concurrent.CountDownLatch;

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

    private final CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void init() throws ServletException {
        final List<String> unknown = unknownParameters();
        if (!unknown.isEmpty()) {
            log.warn("Unknown init-parameters: " + unknown);
        }
        if (features().contains(Feature.ONLINE)) {
            final Thread creator = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        createGeneratorConfig().generate();
                    } catch (Exception e) {
                        log.error("Could not create RAML documentation", e);
                    } finally {
                        latch.countDown();
                    }
                }
            });
            creator.setDaemon(true);
            creator.start();
        }
    }

    protected List<String> knownParameters() {
        return Arrays.asList("ramlLocations", "features", "parentTitle", "baseUri", "baseUriParameters");
    }

    protected List<String> unknownParameters() {
        final List<String> res = new ArrayList<>();
        final Enumeration<String> names = getInitParameterNames();
        while (names.hasMoreElements()) {
            final String name = names.nextElement();
            if (!knownParameters().contains(name)) {
                res.add(name);
            }
        }
        return res;
    }

    protected String ramlLocations() {
        return getInitParameter("ramlLocations");
    }

    protected EnumSet<Feature> features() {
        return Feature.parse(getInitParameter("features"));
    }

    protected String parentTitle() {
        return getInitParameter("parentTitle");
    }

    protected String baseUri() {
        return getInitParameter("baseUri");
    }

    protected String baseUriParameters() {
        return getInitParameter("baseUriParameters");
    }

    protected GeneratorConfig createGeneratorConfig() {
        return new GeneratorConfig(getRamlLocations(), docDir(), features(), parentTitle(), baseUri(), baseUriParameters());
    }

    protected String getRamlLocations() {
        final String locations = ramlLocations();
        return locations == null ? "classpath://api.raml" : locations;
    }

    private File docDir() {
        final File tempDir = new File(System.getProperty("java.io.tmpdir"));
        final String contextPath = getServletContext().getContextPath();
        final boolean isEmpty = contextPath.length() == 0 || contextPath.length() == 1;
        return new File(tempDir, "raml-doc" + (isEmpty ? "/_ROOT" : contextPath));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (!features().contains(Feature.ONLINE)) {
            super.doGet(req, res);
            return;
        }
        try {
            latch.await();
            if (req.getPathInfo() == null) {
                res.sendRedirect(req.getRequestURL().append("/index.html").toString());
                return;
            }
            if (req.getPathInfo().length() == 1) {
                res.sendRedirect(req.getRequestURL().append("index.html").toString());
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
        } catch (InterruptedException e) {
            //ignore
        }
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
}
