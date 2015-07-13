package guru.nidi.raml.doc.st;

import org.raml.model.parameter.AbstractParam;
import org.stringtemplate.v4.AttributeRenderer;

import java.util.Locale;

/**
 *
 */
class ParamRenderer implements AttributeRenderer {
    @Override
    public String toString(Object o, String formatString, Locale locale) {
        final AbstractParam param = (AbstractParam) o;
        String s = "";
        switch (formatString) {
            case "intLimit":
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
            case "strLimit":
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
            default:
                throw new IllegalArgumentException("unknown format '" + formatString + "'");
        }
    }
}
