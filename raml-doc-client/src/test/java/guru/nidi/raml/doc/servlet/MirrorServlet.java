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

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 *
 */
public class MirrorServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if ("png".equals(req.getParameter("q"))) {
            final ServletOutputStream out = res.getOutputStream();
            res.setContentType("image/png");
            copy(getClass().getClassLoader().getResourceAsStream("data/google.png"), out);
        } else {
            final PrintWriter out = res.getWriter();
            if ("html".equals(req.getParameter("q"))) {
                res.setContentType("text/html");
                out.print("<html><body><ul>");
                for (int i = 0; i < 50; i++) {
                    out.println("<li>" + i + "</li>");
                }
                out.print("</ul></body></html>");
            } else if ("json".equals(req.getParameter("q"))) {
                res.setContentType("application/json");
                final ObjectMapper mapper = new ObjectMapper();
                final Map<String, Object> map = new HashMap<>();
                map.put("method", req.getMethod());
                map.put("url", req.getRequestURL().toString());
                map.put("headers", headers(req));
                map.put("query", query(req));
                mapper.writeValue(out, map);
            } else {
                out.println(req.getMethod() + " " + req.getRequestURL());
                headers(req, out);
                query(req, out);
                copy(req.getReader(), out);
            }
        }
        res.flushBuffer();
    }

    private void copy(Reader in, Writer out) throws IOException {
        final char[] buf = new char[10000];
        int read;
        while ((read = in.read(buf)) > 0) {
            out.write(buf, 0, read);
        }
    }

    private void copy(InputStream in, OutputStream out) throws IOException {
        final byte[] buf = new byte[10000];
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

    private Map<String, Object> query(HttpServletRequest req) {
        final Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, String[]> entry : req.getParameterMap().entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
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

    private Map<String, Object> headers(HttpServletRequest req) {
        final Map<String, Object> map = new HashMap<>();
        final Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String name = headerNames.nextElement();
            final List<String> values = new ArrayList<>();
            final Enumeration<String> headers = req.getHeaders(name);
            while (headers.hasMoreElements()) {
                values.add(headers.nextElement());
            }
            map.put(name, values);
        }
        return map;
    }
}
