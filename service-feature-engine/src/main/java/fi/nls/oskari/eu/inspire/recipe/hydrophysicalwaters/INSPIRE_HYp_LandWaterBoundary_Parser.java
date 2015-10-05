package fi.nls.oskari.eu.inspire.recipe.hydrophysicalwaters;

import fi.nls.oskari.eu.inspire.hydrophysicalwaters.INSPIRE_hyp_LandWaterBoundary.LandWaterBoundary;
import fi.nls.oskari.fe.input.format.gml.recipe.JacksonParserRecipe.GML32;
import fi.nls.oskari.fe.iri.Resource;

import java.io.IOException;

public class INSPIRE_HYp_LandWaterBoundary_Parser extends GML32 {

    @Override
    public void parse() throws IOException {
        setLenient(true);
        
        final FeatureOutputContext outputContext = new FeatureOutputContext(
                LandWaterBoundary.QN);

        final Resource geom = outputContext.addDefaultGeometryProperty();
        final Resource beginLifespanVersion = outputContext
                .addOutputStringProperty("beginLifespanVersion");
        final Resource inspireId = outputContext.addOutputProperty("inspireId");
        final Resource endLifespanVersion = outputContext
                .addOutputStringProperty("endLifespanVersion");
        final Resource obj = outputContext.addOutputStringProperty("obj");

        outputContext.build();

        final OutputFeature<LandWaterBoundary> outputFeature = new OutputFeature<LandWaterBoundary>(
                outputContext);

        final InputFeature<LandWaterBoundary> iter = new InputFeature<LandWaterBoundary>(
                LandWaterBoundary.QN, LandWaterBoundary.class);

        while (iter.hasNext()) {
            final LandWaterBoundary feature = iter.next();
            final Resource output_ID = outputContext.uniqueId(feature.id);

            outputFeature.setFeature(feature).setId(output_ID);

            if (feature.geometry != null ) {
                outputFeature.addGeometryProperty(geom,
                        feature.geometry.getGeometry());
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
