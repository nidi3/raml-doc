package guru.nidi.raml.doc.servlet;

import org.apache.commons.collections.iterators.IteratorEnumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class TestServletConfig implements ServletConfig {
    private final String name;
    private final Map<String, String> initParameters = new HashMap<>();

    public TestServletConfig(String name) {
        this.name = name;
    }

    public TestServletConfig withInitParameter(String name, String value) {
        initParameters.put(name, value);
        return this;
    }

    @Override
    public String getServletName() {
        return name;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public String getInitParameter(String name) {
        return initParameters.get(name);
    }

    @Override
    public Enumeration getInitParameterNames() {
        return new IteratorEnumeration(initParameters.keySet().iterator());
    }
}
