package guru.nidi.raml.doc.st;

import org.raml.model.Raml;
import org.stringtemplate.v4.AttributeRenderer;

import java.util.Locale;

/**
 *
 */
class RamlRenderer implements AttributeRenderer {
    @Override
    public String toString(Object o, String formatString, Locale locale) {
        final Raml raml = (Raml) o;
        switch (formatString) {
            case "baseUri":
                if (raml.getProtocols() == null || raml.getProtocols().isEmpty()) {
                    return raml.getBaseUri();
                }
                final int pos = raml.getBaseUri().indexOf("://");
                final String rest = pos < 0 ? raml.getBaseUri() : raml.getBaseUri().substring(pos + 3);
                if (raml.getProtocols().size() == 2) {
                    return "http(s)://" + rest;
                }
                return raml.getProtocols().get(0).toString().toLowerCase() + "://" + rest;
            default:
                throw new IllegalArgumentException("unknown format '" + formatString + "'");
        }
    }
}
