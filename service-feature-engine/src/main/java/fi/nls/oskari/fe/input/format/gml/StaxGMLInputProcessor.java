package fi.nls.oskari.fe.input.format.gml;

import fi.nls.oskari.fe.input.XMLInputProcessor;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMFilterFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;

public class StaxGMLInputProcessor implements XMLInputProcessor {

    XMLInputFactory2 ifac = null;
    SMFilterFactory ffac = null;
    SMInputFactory inf = null;
    private InputStream inp;

    private SMHierarchicCursor rootC;

    public StaxGMLInputProcessor() {
        createFactories();
    }
    private synchronized void createFactories() {
        ifac = (XMLInputFactory2) XMLInputFactory.newInstance();
        ffac = new SMFilterFactory();
        inf = new SMInputFactory(ifac);


    }

    public void close() throws XMLStreamException, IOException {
        rootC.getStreamReader().closeCompletely();
        inp.close();
    }

    public SMInputCursor root() {
        return rootC;
    }

    public void setInput(InputStream inp) throws XMLStreamException {
        this.inp = inp;
        rootC = inf.rootElementCursor(inp);
        rootC.advance(); // note: 2.0 only method; can also call
                         // ".getNext()"

    }

}
