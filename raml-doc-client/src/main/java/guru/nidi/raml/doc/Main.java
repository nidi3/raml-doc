package guru.nidi.raml.doc;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 */
public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java -jar raml-doc-standalone.jar <file>");
            System.exit(1);
        }
        final File file = new File(args[0]);
        if (!file.exists()) {
            System.out.println("File '" + args[0] + "' does not exist");
            System.exit(1);
        }
        try {
            new Generator().generate(file, new File("."));
        } catch (Exception e) {
            System.out.println("Problem generating doc");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
