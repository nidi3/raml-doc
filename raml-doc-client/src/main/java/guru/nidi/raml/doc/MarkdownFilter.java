package guru.nidi.raml.doc;

import com.mitchellbosecke.pebble.extension.Filter;
import org.pegdown.PegDownProcessor;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class MarkdownFilter implements Filter {
    private final PegDownProcessor processor = new PegDownProcessor();

    @Override
    public List<String> getArgumentNames() {
        return null;
    }

    @Override
    public Object apply(Object input, Map<String, Object> args) {
        return input == null ? "" : processor.markdownToHtml(input.toString());
    }

}
