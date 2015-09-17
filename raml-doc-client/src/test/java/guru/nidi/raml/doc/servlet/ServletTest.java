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

        final Wrapper multiRaml = ctx.createWrapper();
        multiRaml.setServletClass(RamlDocServlet.class.getName());
        multiRaml.setName("app");
        multiRaml.addInitParameter("ramlLocations", "classpath://data/basic.raml,classpath://data/second.raml");
        multiRaml.addInitParameter("features", "online,tryout,download");
        multiRaml.addInitParameter("baseUriParameters", "host=$host/$path,path=mirror");
        multiRaml.addInitParameter("customization", "classpath://data");
        ctx.addChild(multiRaml);
        ctx.addServletMapping("/api/*", "app");

        final Wrapper singleRaml = ctx.createWrapper();
        singleRaml.setServletClass(RamlDocServlet.class.getName());
        singleRaml.setName("sapp");
        singleRaml.addInitParameter("ramlLocations", "classpath://data/basic.raml");
        singleRaml.addInitParameter("features", "online");
        singleRaml.addInitParameter("baseUriParameters", "host=$host/$path,path=mirror");
        ctx.addChild(singleRaml);
        ctx.addServletMapping("/sapi/*", "sapp");

        Tomcat.addServlet(ctx, "mirror", new MirrorServlet());
        ctx.addServletMapping("/mirror/v1/*", "mirror");

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
        final HttpGet getIndex = new HttpGet("http://localhost:8080/api/basic/index.html");
        final HttpResponse responseIndex = client.execute(getIndex);
        assertEquals(200, responseIndex.getStatusLine().getStatusCode());
//        if ("/Users/nidi".equals(System.getenv("HOME"))) {
//            Thread.sleep(1000000);
//        }
    }

}
