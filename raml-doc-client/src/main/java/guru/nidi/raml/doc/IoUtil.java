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
package guru.nidi.raml.doc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Pattern;

/**
 *
 */
public class IoUtil {
    private static final String FILE_SYSTEM_UNSAFE = "\\/:*?\"<>|!";
    private static final String URL_UNSAFE = ":/?#[]@!$&'()*+,;=\"\\{}% ";
    private static final Pattern HAS_PROTOCOL = Pattern.compile("\\w+://.+");

    private IoUtil() {
    }

    public static String normalizePath(String path) {
        String res = path;
        int pos;
        while ((pos = res.indexOf("../")) >= 0) {
            int last = res.lastIndexOf("/", pos - 2);
            if (last >= 0) {
                res = res.substring(0, last + 1) + res.substring(pos + 3);
            } else if (pos > 1) {
                res = res.substring(pos + 3);
            } else if (!hasProtocol(path)) {
                break;
            } else {
                throw new IllegalStateException("Invalid path '" + path + "'");
            }
        }
        return res;
    }

    private static boolean hasProtocol(String path) {
        return HAS_PROTOCOL.matcher(path).matches();
    }

    public static String safeName(String name) {
        return safe(name, false);
    }

    public static String safePath(String name) {
        return safe(name, true);
    }

    public static String urlEncodedSafePath(String name) {
        return urlEncoded(safePath(name)).replace("%2F", "/"); //undo url encoding for "/"
    }

    public static String urlEncodedSafeName(String name) {
        return urlEncoded(safeName(name));
    }

    private static String safe(String name, boolean allowSlash) {
        final StringBuilder s = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            final char c = name.charAt(i);
            final boolean ok = (allowSlash && c == '/') || (FILE_SYSTEM_UNSAFE.indexOf(c) < 0 && URL_UNSAFE.indexOf(c) < 0);
            s.append(ok ? c : '-');
        }
        return s.toString();
    }

    public static String urlEncoded(String name) {
        try {
            return URLEncoder.encode(name, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("Cannot happen");
        }
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        final byte[] buf = new byte[1000];
        int read;
        while ((read = in.read(buf)) > 0) {
            out.write(buf, 0, read);
        }
    }
}
