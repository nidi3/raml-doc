package guru.nidi.raml.doc;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 *
 */
public class MainTest {
    @Test
    public void basic() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new Generator().generate(new File("src/test/resources/basic.raml"), new OutputStreamWriter(baos));
        System.out.println(baos.toString());
        try (final OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File("target/out.html")))) {
            writer.write(baos.toString());
        }
    }
}
