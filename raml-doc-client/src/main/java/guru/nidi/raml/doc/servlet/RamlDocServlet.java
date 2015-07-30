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

import guru.nidi.raml.doc.st.Generator;
import guru.nidi.raml.loader.RamlLoaders;
import org.raml.model.Raml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
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
        if (enabled()) {
            final Thread creator = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final File outputDir = docDir();
                        outputDir.mkdirs();
                        final Raml raml = loadRaml(getRamlLocation());
                        baseDir = new Generator()
                                .tryOut(tryOut() ? getBaseUri(raml) : null)
                                .generate(raml, outputDir);
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

    protected boolean enabled() {
        return true;
    }

    protected String ramlLocation() {
        return getInitParameter("ramlLocation");
    }

    protected boolean tryOut() {
        return Boolean.parseBoolean(getInitParameter("tryOut"));
    }

    protected String baseUri() {
        return getInitParameter("baseUri");
    }

    protected String baseUriParameters() {
        return getInitParameter("baseUriParameters");
    }

    protected String getRamlLocation() {
        final String location = ramlLocation();
        return location == null ? "classpath://api.raml" : location;
    }

    protected Raml loadRaml(String ramlLocation) throws IOException {
        try {
            return RamlLoaders.absolutely().load(ramlLocation);
        } catch (Exception e) {
            throw new IOException("No raml found at location '" + ramlLocation + "'");
        }
    }

    protected String getBaseUri(Raml raml) {
        final String definedBaseUri = baseUri();
        if (definedBaseUri != null) {
            return definedBaseUri;
        }
        String baseUri = raml.getBaseUri().replace("{version}", raml.getVersion());
        final String baseUriParameters = baseUriParameters();
        if (baseUriParameters != null) {
            for (final String param : baseUriParameters.split(",")) {
                final String[] keyValue = param.split("=");
                if (keyValue.length != 2) {
                    throw new IllegalArgumentException("baseUriParameters must be of the form 'key1=value1,key2=value2,...' but is '" + baseUriParameters + "'");
                }
                baseUri = baseUri.replace("{" + keyValue[0] + "}", keyValue[1]);
            }
        }
        if (baseUri.contains("{")) {
            throw new IllegalArgumentException("Unresolved baseUri: '" + baseUri + "'. Use 'baseUri' or 'baseUriParameters' init-param to specify it.");
        }
        return baseUri;
    }

    private File docDir() {
        final File tempDir = new File(System.getProperty("java.io.tmpdir"));
        return new File(tempDir, "raml-doc/" + getServletName());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (!enabled()) {
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
