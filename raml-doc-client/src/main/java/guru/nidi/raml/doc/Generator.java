package guru.nidi.raml.doc;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.extension.escaper.EscaperExtension;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import org.raml.model.Raml;
import org.raml.parser.loader.FileResourceLoader;
import org.raml.parser.visitor.RamlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class Generator {
    public void generate(File file, Writer out) throws Exception {
        final Raml raml = new RamlDocumentBuilder(new FileResourceLoader(file)).build(new FileInputStream(file), file.getAbsolutePath());

        PebbleEngine engine = new PebbleEngine();
        engine.addExtension(new MyExtension());
        final EscaperExtension escaper = engine.getExtension(EscaperExtension.class);
        escaper.addSafeFilter("markdown");

        PebbleTemplate compiledTemplate = engine.getTemplate("main.html");

        Map<String, Object> context = new HashMap<>();
        context.put("raml", raml);
        context.put("str", new StringFormatter(raml));
        context.put("util", new Util(raml));

        compiledTemplate.evaluate(out, context);
    }
}
