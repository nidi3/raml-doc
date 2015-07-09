package guru.nidi.raml.doc;

import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Filter;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class MyExtension extends AbstractExtension {
    private final Map<String, Filter> filters = new HashMap<String, Filter>() {{
        put("markdown", new MarkdownFilter());
        put("boolean", new BooleanFilter());
    }};

    @Override
    public Map<String, Filter> getFilters() {
        return filters;
    }
}
