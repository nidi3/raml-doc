package guru.nidi.raml.doc.servlet;

import guru.nidi.raml.doc.st.Generator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.concurrent.CountDownLatch;

/**
 *
 */
public class RamlDocServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(RamlDocServlet.class);
    private final CountDownLatch latch = new CountDownLatch(1);
    private File baseDir;

    @Override
    public void init() throws ServletException {
        final Thread creator = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String location = getInitParameter("ramlLocation");
                    final File outputDir = docDir();
                    outputDir.mkdirs();
                    baseDir = new Generator().generate(location == null ? "api.raml" : location, outputDir);
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

    private File docDir() {
        final File tempDir = new File(System.getProperty("java.io.tmpdir"));
        return new File(tempDir, "raml-doc/" + getServletName());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            latch.await();
            if (req.getPathInfo() == null) {
                res.sendRedirect(req.getRequestURL().append("/index.html").toString());
                return;
            }
            final File source = new File(baseDir, req.getPathInfo());
            if (!source.exists()) {
                final PrintWriter writer = res.getWriter();
                writer.write("File not found.");
                writer.flush();
                res.flushBuffer();
            } else {
                try (final InputStream in = new FileInputStream(source);
                     final OutputStream out = new BufferedOutputStream(res.getOutputStream())) {
                    copy(in, out);
                }
            }
        } catch (InterruptedException e) {
            //ignore
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
