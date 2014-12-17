package fi.nls.oskari.eu.inspire.recipe.cadastralparcels;

import java.io.IOException;

import fi.nls.oskari.eu.inspire.cadastralparcels.INSPIRE_cp_CadastralBoundary.CadastralBoundary;
import fi.nls.oskari.fe.input.format.gml.recipe.JacksonParserRecipe.GML32;
import fi.nls.oskari.fe.iri.Resource;

public class INSPIRE_CP_CadastralBoundary_Parser extends GML32 {

    @Override
    public void parse() throws IOException {

        final FeatureOutputContext outputContext = new FeatureOutputContext(
                CadastralBoundary.QN);

        final Resource geom = outputContext.addDefaultGeometryProperty();
        final Resource beginLifespanVersion = outputContext
                .addOutputStringProperty("beginLifespanVersion");
        final Resource inspireId = outputContext.addOutputProperty("inspireId");
        final Resource endLifespanVersion = outputContext
                .addOutputStringProperty("endLifespanVersion");
        final Resource obj = outputContext.addOutputStringProperty("obj");

        outputContext.build();

        final OutputFeature<CadastralBoundary> outputFeature = new OutputFeature<CadastralBoundary>(
                outputContext);

        final InputFeature<CadastralBoundary> iter = new InputFeature<CadastralBoundary>(
                CadastralBoundary.QN, CadastralBoundary.class);

        while (iter.hasNext()) {
            final CadastralBoundary feature = iter.next();
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
