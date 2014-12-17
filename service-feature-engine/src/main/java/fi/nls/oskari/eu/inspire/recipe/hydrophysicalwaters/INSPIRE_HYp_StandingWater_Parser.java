package fi.nls.oskari.eu.inspire.recipe.hydrophysicalwaters;



import java.io.IOException;

import fi.nls.oskari.eu.inspire.hydrophysicalwaters.INSPIRE_hyp_StandingWater.StandingWater;
import fi.nls.oskari.fe.input.format.gml.recipe.JacksonParserRecipe.GML32;
import fi.nls.oskari.fe.iri.Resource;

public class INSPIRE_HYp_StandingWater_Parser extends GML32 {

    @Override
    public void parse() throws IOException {

        final FeatureOutputContext outputContext = new FeatureOutputContext(
                StandingWater.QN);

        final Resource geom = outputContext.addDefaultGeometryProperty();
        final Resource beginLifespanVersion = outputContext
                .addOutputStringProperty("beginLifespanVersion");
        final Resource inspireId = outputContext.addOutputProperty("inspireId");
        final Resource endLifespanVersion = outputContext
                .addOutputStringProperty("endLifespanVersion");
        final Resource obj = outputContext.addOutputStringProperty("obj");

        outputContext.build();

        final OutputFeature<StandingWater> outputFeature = new OutputFeature<StandingWater>(
                outputContext);

        final InputFeature<StandingWater> iter = new InputFeature<StandingWater>(
                StandingWater.QN, StandingWater.class);

        while (iter.hasNext()) {
            final StandingWater feature = iter.next();
            final Resource output_ID = outputContext.uniqueId(feature.id);

            outputFeature.setFeature(feature).setId(output_ID);

            if (feature.geometry != null && feature.geometry.geometry != null) {
                outputFeature.addGeometryProperty(geom,
                        feature.geometry.geometry);
            }

            outputFeature
                    .addProperty(beginLifespanVersion,
                            feature.beginLifespanVersion)
                    .addProperty(inspireId, feature.inspireId)
                    .addProperty(endLifespanVersion, feature.endLifespanVersion);

            outputFeature.addProperty(obj, feature);

            outputFeature.build();

        }

    }

}
