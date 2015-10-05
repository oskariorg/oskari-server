package fi.nls.oskari.fe.engine;

import fi.nls.oskari.fe.input.InputProcessor;
import fi.nls.oskari.fe.input.format.gml.recipe.ParserRecipe;
import fi.nls.oskari.fe.output.OutputProcessor;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

public interface FeatureEngine {

	public void setInputProcessor(InputProcessor inputProcessor);

	public void setOutputProcessor(OutputProcessor outputProcessor);
	public ParserRecipe getRecipe();
	
	public void process() throws IOException, XMLStreamException;
}
