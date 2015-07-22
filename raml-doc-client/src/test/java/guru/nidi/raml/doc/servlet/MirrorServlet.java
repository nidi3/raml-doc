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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Map;

/**
 *
 */
public class MirrorServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        final PrintWriter out = res.getWriter();
        out.println(req.getMethod() + " " + req.getRequestURL());
        headers(req, out);
        query(req, out);
        copy(req.getReader(), out);
        res.flushBuffer();
    }

    private void copy(Reader in, Writer out) throws IOException {
        final char[] buf = new char[10000];
        int read;
        while ((read = in.read(buf)) > 0) {
            out.write(buf, 0, read);
        }
    }

    private void query(HttpServletRequest req, PrintWriter out) {
        out.println("query");
        for (Map.Entry<String, String[]> entry : req.getParameterMap().entrySet()) {
            out.print(entry.getKey() + ": ");
            for (String s : entry.getValue()) {
                out.print("'" + s + "', ");
            }
            out.println();
        }
        out.println();
    }

    private void headers(HttpServletRequest req, PrintWriter out) {
        out.println("headers");
        final Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String name = headerNames.nextElement();
            out.print(name + ": ");
            final Enumeration<String> headers = req.getHeaders(name);
            while (headers.hasMoreElements()) {
                out.print("'" + headers.nextElement() + "', ");
            }
            out.println();
        }
        out.println();
    }
}