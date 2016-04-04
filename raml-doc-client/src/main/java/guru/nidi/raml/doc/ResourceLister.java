package guru.nidi.raml.doc;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ResourceLister {
    private static final File BASE = new File("src/main/resources/guru/nidi/raml/doc");
    private static final String DIR = "static";

    public static void main(String[] args) throws IOException {
        try (final PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(BASE, DIR + "-files.lst"))))) {
            for (final String file : files(new File(BASE, DIR))) {
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
                    listFiles(file, path + file.getName() + "/", list);
                }
            }
        }
    }
}
