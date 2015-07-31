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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 *
 */
public class RamlDocServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(RamlDocServlet.class);
    private final CountDownLatch latch = new CountDownLatch(1);
    private File baseDir;
    private static final Map<String, String> mimeTypes = new HashMap<String, String>() {{
        put("html", "text/html");
        put("css", "text/css");
        put("js", "text/javascript");
    }};

    @Override
    public void init() throws ServletException {
        if (features().contains(Feature.ONLINE)) {
            final Thread creator = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final GeneratorConfig config = createGeneratorConfig();
                        config.loadRaml();
                        baseDir = config.getEffectiveTarget();
                        config.generate();
                    } catch (IOException e) {
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

    protected String ramlLocation() {
        return getInitParameter("ramlLocation");
    }

    protected EnumSet<Feature> features() {
        return Feature.parse(getInitParameter("features"));
    }

    protected String baseUri() {
        return getInitParameter("baseUri");
    }

    protected String baseUriParameters() {
        return getInitParameter("baseUriParameters");
    }

    protected GeneratorConfig createGeneratorConfig() {
        return new GeneratorConfig(getRamlLocation(), docDir(), features(), baseUri(), baseUriParameters());
    }

    protected String getRamlLocation() {
        final String location = ramlLocation();
        return location == null ? "classpath://api.raml" : location;
    }

    private File docDir() {
        final File tempDir = new File(System.getProperty("java.io.tmpdir"));
        return new File(tempDir, "raml-doc/" + getServletName());
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
            final File source = new File(baseDir, req.getPathInfo());
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
