package fi.nls.oskari.eu.elf.recipe.tn;

import java.io.IOException;

import fi.nls.oskari.eu.elf.roadtransportnetwork.masterlod1.RoadLink;
import fi.nls.oskari.fe.input.format.gml.recipe.JacksonParserRecipe.GML32;
import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.fe.schema.XSDDatatype;

/* PoC Streaming Parser to Match Groovy Parser in Java 7 */
public class ELF_TN_RoadLink extends GML32 {

    @Override
    public void parse() throws IOException {

        final FeatureOutputContext outputContext = new FeatureOutputContext(
                RoadLink.QN);

        final Resource geom = outputContext.addDefaultGeometryProperty();
        final Resource gn = outputContext.addOutputProperty("geographicalName");
        final Resource validFrom = outputContext
                .addOutputStringProperty("validFrom");
        final Resource validTo = outputContext
                .addOutputStringProperty("validTo");
        final Resource fictitious = outputContext.addOutputProperty(
                XSDDatatype.XSDboolean, "fictitious");
        final Resource beginLifespanVersion = outputContext
                .addOutputStringProperty("beginLifespanVersion");
        final Resource inspireId = outputContext.addOutputProperty("inspireId");
        final Resource endLifespanVersion = outputContext
                .addOutputStringProperty("endLifespanVersion");

        outputContext.build();

        final OutputFeature<RoadLink> outputFeature = new OutputFeature<RoadLink>(
                outputContext);

        final InputFeature<RoadLink> iter = new InputFeature<RoadLink>(
                RoadLink.QN, RoadLink.class);

        while (iter.hasNext()) {
            final RoadLink feature = iter.next();
            final Resource output_ID = outputContext.uniqueId(feature.id);

            outputFeature.setFeature(feature).setId(output_ID);

            outputFeature.addGeometryProperty(geom,
                    feature.centrelineGeometry.geometry);

            outputFeature
                    .addProperty(gn, feature.geographicalName)
                    .addProperty(validFrom, feature.validFrom)
                    .addProperty(validTo, feature.validTo)
                    .addProperty(fictitious, feature.fictitious)
                    .addProperty(beginLifespanVersion,
                            feature.beginLifespanVersion)
                    .addProperty(inspireId, feature.inspireId)
                    .addProperty(endLifespanVersion, feature.endLifespanVersion);

            outputFeature.build();

        }

    }

}