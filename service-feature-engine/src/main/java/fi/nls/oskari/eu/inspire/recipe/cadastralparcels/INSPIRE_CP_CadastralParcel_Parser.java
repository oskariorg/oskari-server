package fi.nls.oskari.eu.inspire.recipe.cadastralparcels;

import fi.nls.oskari.eu.inspire.cadastralparcels.INSPIRE_cp_CadastralParcel.CadastralParcel;
import fi.nls.oskari.fe.input.format.gml.recipe.JacksonParserRecipe.GML32;
import fi.nls.oskari.fe.iri.Resource;

import java.io.IOException;

public class INSPIRE_CP_CadastralParcel_Parser extends GML32 {

    @Override
    public void parse() throws IOException {
        setLenient(true);

        final FeatureOutputContext outputContext = new FeatureOutputContext(
                CadastralParcel.QN);

        final Resource geom = outputContext.addDefaultGeometryProperty();
        final Resource beginLifespanVersion = outputContext
                .addOutputStringProperty("beginLifespanVersion");
        final Resource inspireId = outputContext.addOutputProperty("inspireId");
        final Resource endLifespanVersion = outputContext
                .addOutputStringProperty("endLifespanVersion");
        final Resource obj = outputContext.addOutputStringProperty("obj");

        outputContext.build();

        final OutputFeature<CadastralParcel> outputFeature = new OutputFeature<CadastralParcel>(
                outputContext);

        final InputFeature<CadastralParcel> iter = new InputFeature<CadastralParcel>(
                CadastralParcel.QN, CadastralParcel.class);

        while (iter.hasNext()) {
            final CadastralParcel feature = iter.next();
            final Resource output_ID = outputContext.uniqueId(feature.id);

            outputFeature.setFeature(feature).setId(output_ID);

            if (feature.geometry != null) {
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
