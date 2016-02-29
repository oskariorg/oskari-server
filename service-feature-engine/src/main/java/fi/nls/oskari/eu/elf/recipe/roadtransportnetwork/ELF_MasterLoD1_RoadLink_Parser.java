package fi.nls.oskari.eu.elf.recipe.roadtransportnetwork;

import fi.nls.oskari.eu.elf.roadtransportnetwork.ELF_TNRO_RoadLink.RoadLink;
import fi.nls.oskari.fe.input.format.gml.recipe.JacksonParserRecipe.GML32;
import fi.nls.oskari.fe.iri.Resource;

import java.io.IOException;

public class ELF_MasterLoD1_RoadLink_Parser extends GML32 {

    @Override
    public void parse() throws IOException {
        setLenient(true);

        final FeatureOutputContext outputContext = new FeatureOutputContext(
                RoadLink.QN);

        final Resource geom = outputContext.addDefaultGeometryProperty();
        final Resource gn = outputContext.addOutputProperty("name");
        final Resource beginLifespanVersion = outputContext
                .addOutputStringProperty("beginLifespanVersion");
        final Resource inspireId = outputContext.addOutputProperty("inspireId");
        final Resource endLifespanVersion = outputContext
                .addOutputStringProperty("endLifespanVersion");

        final Resource obj = outputContext.addOutputStringProperty("obj");

        outputContext.build();

        final OutputFeature<RoadLink> outputFeature = new OutputFeature<RoadLink>(
                outputContext);

        final InputFeature<RoadLink> iter = new InputFeature<RoadLink>(
                RoadLink.QN, RoadLink.class);

        while (iter.hasNext()) {
            final RoadLink feature = iter.next();
            final Resource output_ID = outputContext.uniqueId(feature.id);

            outputFeature.setFeature(feature).setId(output_ID);

            if (feature.centrelineGeometry != null) {
                outputFeature.addGeometryProperty(geom,
                        feature.centrelineGeometry.getGeometry());
            }

            outputFeature
                    .addProperty(gn, feature.geographicalName)
                    .addProperty(beginLifespanVersion,
                            feature.beginLifespanVersion)
                    .addProperty(inspireId, feature.inspireId)
                    .addProperty(endLifespanVersion, feature.endLifespanVersion);

            outputFeature.addProperty(obj, feature);

            outputFeature.build();

        }

    }
}
