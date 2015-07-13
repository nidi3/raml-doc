package guru.nidi.raml.doc.st;

import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.MapModelAdaptor;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

import java.util.Map;

/**
 *
 */
class EntrySetMapModelAdaptor extends MapModelAdaptor {
    @Override
    public Object getProperty(Interpreter interp, ST self, Object o, Object property, String propertyName) throws STNoSuchPropertyException {
        if ("entrySet".equals(propertyName)) {
            return ((Map<?, ?>) o).entrySet();
        }
        return super.getProperty(interp, self, o, property, propertyName);
    }
}
