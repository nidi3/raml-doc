package guru.nidi.raml.doc.st;

import org.pegdown.PegDownProcessor;
import org.raml.model.Raml;
import org.stringtemplate.v4.AttributeRenderer;

import java.util.Locale;

/**
 *
 */
class StringRenderer implements AttributeRenderer {
    private final PegDownProcessor processor;
    private final Raml raml;

    public StringRenderer(Raml raml) {
        this.raml = raml;
        processor = new PegDownProcessor();
    }

    @Override
    public String toString(Object o, String formatString, Locale locale) {
        String s = (String) o;
        if (formatString == null) {
            return s;
        }
        for (String fmt : formatString.split(",")) {
            s = format(s, fmt);
        }
        return s;
    }

    private String format(String s, String formatString) {
        switch (formatString) {
            case "markdown":
                return processor.markdownToHtml(s);
            case "summary":
                final int pos = s.indexOf('.');
                return pos < 0 ? s : s.substring(0, pos + 1);
            case "schema":
                final String refSchema = raml.getConsolidatedSchemas().get(s);
                return refSchema == null ? s : refSchema;
            default:
                throw new IllegalArgumentException("unknown format '" + formatString + "'");
        }
    }
}
