package fi.nls.oskari.eu.elf.recipe.hydronetwork;

import fi.nls.oskari.eu.elf.hydronetwork.ELF_MasterLoD1_WatercourseLink.WatercourseLink;
import fi.nls.oskari.fe.input.format.gml.recipe.JacksonParserRecipe.GML32;
import fi.nls.oskari.fe.iri.Resource;

import java.io.IOException;

public class ELF_MasterLoD1_WatercourseLink_Parser extends GML32 {

    @Override
    public void parse() throws IOException {
        setLenient(true);

        final FeatureOutputContext outputContext = new FeatureOutputContext(
                WatercourseLink.QN);

        final Resource geom = outputContext.addDefaultGeometryProperty();
        final Resource gn = outputContext.addOutputProperty("name");
        final Resource beginLifespanVersion = outputContext
                .addOutputStringProperty("beginLifespanVersion");
        final Resource inspireId = outputContext.addOutputProperty("inspireId");
        final Resource endLifespanVersion = outputContext
                .addOutputStringProperty("endLifespanVersion");
        final Resource obj = outputContext.addOutputStringProperty("obj");

        outputContext.build();

        final OutputFeature<WatercourseLink> outputFeature = new OutputFeature<WatercourseLink>(
                outputContext);

        final InputFeature<WatercourseLink> iter = new InputFeature<WatercourseLink>(
                WatercourseLink.QN, WatercourseLink.class);

        while (iter.hasNext()) {
            final WatercourseLink feature = iter.next();
            final Resource output_ID = outputContext.uniqueId(feature.id);

            outputFeature.setFeature(feature).setId(output_ID);

            if (feature.centrelineGeometry != null
                    && feature.centrelineGeometry.getGeometry() != null) {

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
