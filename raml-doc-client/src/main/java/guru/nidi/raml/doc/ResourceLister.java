/*
 * Copyright © 2015 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.raml.doc;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ResourceLister {
    private static final String BASE = "src/main/resources/guru/nidi/raml/doc";
    private static final String DIR = "static";

    public static void main(String[] args) throws IOException {
        final File baseDir = new File(args[0], BASE);
        try (final PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(baseDir, DIR + "-files.lst"))))) {
            for (final String file : files(new File(baseDir, DIR))) {
                out.println(file);
            }
        }
    }

    private static List<String> files(File baseDir) throws FileNotFoundException {
        final List<String> list = new ArrayList<>();
        listFiles(baseDir, "", list);
        return list;
    }

    private static void listFiles(File baseDir, String path, List<String> list) throws FileNotFoundException {
        final File[] files = baseDir.listFiles();
        if (files != null) {
            for (final File file : files) {
                if (file.isFile()) {
                    list.add(path + file.getName());
                } else if (file.isDirectory()) {
                    list.add(path + file.getName() + "/");
                    listFiles(file, path + file.getName() + "/", list);
                }
            }
        }
    }
}
