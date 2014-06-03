package fi.nls.oskari.fe.engine;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import fi.nls.oskari.fe.input.InputProcessor;
import fi.nls.oskari.fe.output.OutputProcessor;

public interface FeatureEngine {

	public void setInputProcessor(InputProcessor inputProcessor);

	public void setOutputProcessor(OutputProcessor outputProcessor);
	
	public void process() throws IOException, XMLStreamException;
}
