package fi.nls.oskari.fe.input;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;

public interface InputProcessor {

	public void setInput(final InputStream inp) throws XMLStreamException;

	public void close() throws XMLStreamException, IOException;
}
