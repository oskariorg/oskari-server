package fi.nls.oskari.fe.engine;

import fi.nls.oskari.fe.input.InputProcessor;
import fi.nls.oskari.fe.input.format.gml.recipe.ParserRecipe;
import fi.nls.oskari.fe.output.OutputProcessor;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

public class BasicFeatureEngine implements FeatureEngine {

    private InputProcessor inputProcessor;
    private OutputProcessor outputProcessor;

    ParserRecipe recipe;

    public ParserRecipe getRecipe() {
        return recipe;
    }

    public void process() throws IOException, XMLStreamException {

        try {
            outputProcessor.begin();

            try {

                recipe.setInputOutput(inputProcessor, outputProcessor);
                recipe.parse();

            } finally {
                inputProcessor.close();
            }

            outputProcessor.flush();
        } finally {
            outputProcessor.end();
        }

    }

    public void setInputProcessor(InputProcessor inputProcessor) {
        this.inputProcessor = inputProcessor;
    }

    public void setOutputProcessor(OutputProcessor outputProcessor) {
        this.outputProcessor = outputProcessor;
    }

    public void setRecipe(ParserRecipe recipe) {
        this.recipe = recipe;
    }

}
