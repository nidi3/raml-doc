package guru.nidi.raml.doc.st;

import org.raml.model.Raml;
import org.raml.model.parameter.AbstractParam;
import org.raml.parser.loader.FileResourceLoader;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupDir;

import java.io.*;
import java.util.Map;

/**
 *
 */
public class Generator {
    public void generate(File input, File target) throws Exception {
        final Raml raml = new RamlDocumentBuilder(new FileResourceLoader(input.getParentFile()))
                .build(new FileInputStream(input), input.getName());

        final STGroupDir group = new STGroupDir("st", '$', '$');
        group.registerModelAdaptor(Map.class, new EntrySetMapModelAdaptor());
        group.registerRenderer(String.class, new StringRenderer(raml));
        group.registerRenderer(Boolean.class, new BooleanRenderer());
        group.registerRenderer(AbstractParam.class, new ParamRenderer());
        group.registerRenderer(Raml.class, new RamlRenderer());
        final ST main = group.getInstanceOf("main/main");
        main.add("raml", raml);
        main.add("util", new Util(raml));

        try (final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new File(target, raml.getTitle() + "2.html")))) {
            out.write(main.render());
        }

        try (final InputStream in = getClass().getResourceAsStream("/style.css");
             final FileOutputStream out = new FileOutputStream(new File(target, "style.css"))) {
            copy(in, out);
        }
    }

    private void copy(InputStream in, OutputStream out) throws IOException {
        final byte[] buf = new byte[1000];
        int read;
        while ((read = in.read(buf)) > 0) {
            out.write(buf, 0, read);
        }
    }

}
