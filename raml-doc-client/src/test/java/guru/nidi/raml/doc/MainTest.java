package guru.nidi.raml.doc;

import com.github.fge.jsonschema.core.load.SchemaLoader;
import com.github.fge.jsonschema.core.tree.SchemaTree;
import guru.nidi.raml.doc.st.Generator;
import org.junit.Test;

import java.io.File;
import java.net.URI;

/**
 *
 */
public class MainTest {
    @Test
    public void basic() throws Exception {
        final SchemaTree tree = new SchemaLoader().get(URI.create("file:///" + new File("src/test/resources/schema.json").getAbsolutePath()));
        new Generator().generate(new File("src/test/resources/basic.raml"), new File("target"));
    }
}
