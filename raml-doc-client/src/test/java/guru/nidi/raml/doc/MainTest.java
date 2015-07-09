package guru.nidi.raml.doc;

import org.junit.Test;

import java.io.File;

/**
 *
 */
public class MainTest {
    @Test
    public void basic() throws Exception {
        new Generator().generate(new File("src/test/resources/basic.raml"), new File("target"));
    }
}
