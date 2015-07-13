package guru.nidi.raml.doc.st;

import org.stringtemplate.v4.AttributeRenderer;

import java.util.Locale;

/**
 *
 */
class BooleanRenderer implements AttributeRenderer {
    @Override
    public String toString(Object o, String formatString, Locale locale) {
        if (formatString == null) {
            return o.toString();
        }
        switch (formatString) {
            case "x":
                return o != null && (Boolean) o ? "X" : "";
            default:
                throw new IllegalArgumentException("unknown format '" + formatString + "'");
        }
    }
}
