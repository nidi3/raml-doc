package guru.nidi.raml.doc;

import com.mitchellbosecke.pebble.extension.Filter;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class BooleanFilter implements Filter {
    @Override
    public List<String> getArgumentNames() {
        return null;
    }

    @Override
    public Object apply(Object input, Map<String, Object> args) {
        return input != null && ((Boolean) input) ? "X" : "";
    }

}
