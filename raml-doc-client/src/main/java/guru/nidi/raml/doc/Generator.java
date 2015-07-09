package guru.nidi.raml.doc;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.extension.escaper.EscaperExtension;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import org.raml.model.Raml;
import org.raml.parser.loader.FileResourceLoader;
import org.raml.parser.visitor.RamlDocumentBuilder;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class Generator {
    public void generate(File input, File target) throws Exception {
        final Raml raml = new RamlDocumentBuilder(new FileResourceLoader(input)).build(new FileInputStream(input), input.getAbsolutePath());

        PebbleEngine engine = new PebbleEngine();
        engine.addExtension(new MyExtension());
        final EscaperExtension escaper = engine.getExtension(EscaperExtension.class);
        escaper.addSafeFilter("markdown");

        PebbleTemplate compiledTemplate = engine.getTemplate("main.html");

        Map<String, Object> context = new HashMap<>();
        context.put("raml", raml);
        context.put("str", new StringFormatter(raml));
        context.put("util", new Util(raml));

        try (final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new File(target, raml.getTitle() + ".html")))) {
            compiledTemplate.evaluate(out, context);
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
