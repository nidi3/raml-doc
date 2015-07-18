package guru.nidi.raml.doc.servlet;

import org.apache.catalina.*;
import org.apache.catalina.startup.Tomcat;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.tomcat.JarScanFilter;
import org.apache.tomcat.JarScanType;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.JarScannerCallback;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class ServletTest {
    private static Tomcat tomcat;

    @BeforeClass
    public static void init() throws LifecycleException, ServletException {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.setBaseDir(".");
        Context ctx = tomcat.addWebapp("", "src/test");
        ctx.setJarScanner(new JarScanner() {
            @Override
            public void scan(JarScanType jarScanType, ServletContext servletContext, JarScannerCallback jarScannerCallback) {
            }

            @Override
            public JarScanFilter getJarScanFilter() {
                return null;
            }

            @Override
            public void setJarScanFilter(JarScanFilter jarScanFilter) {
            }
        });
        ((Host) ctx.getParent()).setAppBase("");

        final RamlDocServlet servlet = new RamlDocServlet();
        servlet.init(new TestServletConfig("ramlDoc").withInitParameter("ramlLocation", "basic.raml"));
        Tomcat.addServlet(ctx, "app", servlet);
        ctx.addServletMapping("/api/*", "app");

        tomcat.start();
        Server server = tomcat.getServer();
        server.start();
    }

    @AfterClass
    public static void end() throws LifecycleException {
        if (tomcat.getServer() != null && tomcat.getServer().getState() != LifecycleState.DESTROYED) {
            if (tomcat.getServer().getState() != LifecycleState.STOPPED) {
                tomcat.stop();
            }
            tomcat.destroy();
        }
    }

    @Test
    public void simple() throws IOException, InterruptedException {
        final HttpClient client = HttpClientBuilder.create().build();
        final HttpGet get = new HttpGet("http://localhost:8080/api");
        final HttpResponse response = client.execute(get);
        assertEquals(200, response.getStatusLine().getStatusCode());
        final HttpGet getIndex = new HttpGet("http://localhost:8080/api/index.html");
        final HttpResponse responseIndex = client.execute(getIndex);
        assertEquals(200, responseIndex.getStatusLine().getStatusCode());
//        Thread.sleep(1000000);
    }

}
