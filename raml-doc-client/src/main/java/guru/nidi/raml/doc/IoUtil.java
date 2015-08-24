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

/**
 *
 */
public class IoUtil {
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
            } else {
                throw new IllegalStateException("Invalid path '" + path + "'");
            }
        }
        return res;
    }
}
