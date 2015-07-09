package guru.nidi.raml.doc;

import org.raml.model.Raml;
import org.raml.model.parameter.AbstractParam;
import org.raml.model.parameter.UriParameter;

/**
 *
 */
public class StringFormatter {
    private final Raml raml;

    public StringFormatter(Raml raml) {
        this.raml = raml;
    }

    public String getBaseUri() {
        if (raml.getProtocols() == null || raml.getProtocols().isEmpty()) {
            return raml.getBaseUri();
        }
        final int pos = raml.getBaseUri().indexOf("://");
        final String rest = pos < 0 ? raml.getBaseUri() : raml.getBaseUri().substring(pos + 3);
        if (raml.getProtocols().size() == 2) {
            return "http(s)://" + rest;
        }
        return raml.getProtocols().get(0).toString().toLowerCase() + "://" + rest;
    }

    public String stringLen(UriParameter param) {
        return stringLenImpl(param);
    }

    public String intLimit(UriParameter param) {
        return intLimitImpl(param);
    }

    public String stringLenImpl(AbstractParam param) {
        String s = "";
        if (param.getMinLength() != null) {
            s += param.getMinLength() + "<=";
        }
        if (param.getMinLength() != null || param.getMaxLength() != null) {
            s += "length";
        }
        if (param.getMaxLength() != null) {
            s += "<=" + param.getMaxLength();
        }
        return s;
    }

    public String intLimitImpl(AbstractParam param) {
        String s = "";
        if (param.getMinimum() != null) {
            s += param.getMinimum() + "<=";
        }
        if (param.getMinimum() != null || param.getMaximum() != null) {
            s += "x";
        }
        if (param.getMaximum() != null) {
            s += "<=" + param.getMaximum();
        }
        return s;
    }
}
