package fi.nls.oskari.fe.input;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;

public interface InputProcessor {

	public void setInput(final InputStream inp) throws XMLStreamException;

	public void close() throws XMLStreamException, IOException;
}
