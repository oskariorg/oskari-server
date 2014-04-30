package fi.nls.oskari.fe.input.format.gml;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMFilterFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;

import fi.nls.oskari.fe.input.XMLInputProcessor;

public class StaxGMLInputProcessor implements XMLInputProcessor {

	SMInputFactory inf;
	SMFilterFactory ffac = new SMFilterFactory();
	private InputStream inp;

	private SMHierarchicCursor rootC;

	public StaxGMLInputProcessor() {
		XMLInputFactory2 ifac = (XMLInputFactory2) XMLInputFactory
				.newInstance();
		ifac.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
		ifac.configureForSpeed();

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
