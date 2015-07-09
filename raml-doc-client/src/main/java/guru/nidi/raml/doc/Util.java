package guru.nidi.raml.doc;

import org.raml.model.Raml;
import org.raml.model.Resource;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 */
public class Util {
    private final Raml raml;

    public Util(Raml raml) {
        this.raml = raml;
    }

    public Map<String, Resource> getAllResources() {
        final Map<String, Resource> res = new LinkedHashMap<>();
        for (Map.Entry<String, Resource> entry : raml.getResources().entrySet()) {
            res.put(entry.getKey(), entry.getValue());
            addSubResources(entry.getKey(), entry.getValue(), res);
        }
        return res;
    }

    private void addSubResources(String path, Resource resource, Map<String, Resource> res) {
        for (Map.Entry<String, Resource> entry : resource.getResources().entrySet()) {
            res.put(path + entry.getKey(), entry.getValue());
            addSubResources(path + entry.getKey(), entry.getValue(), res);
        }
    }
}
