package guru.nidi.raml.doc.st;

import biz.gabrys.lesscss.compiler.CompilerException;
import biz.gabrys.lesscss.compiler.LessCompilerImpl;
import org.junit.Test;

import java.io.File;

/**
 *
 */
public class LessTest {
    @Test
    public void simple() throws CompilerException {
        final LessCompilerImpl compiler = new LessCompilerImpl();
        final String compile = compiler.compile(new File("src/test/resources/test.less"));
        System.out.println(compile);
    }
}
