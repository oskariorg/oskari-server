package fi.nls.oskari.fe.input.format.gml.tn;

import java.io.IOException;

import fi.nls.oskari.eu.inspire.gmlas.roadtransportnetwork.RoadLink;
import fi.nls.oskari.fe.input.format.gml.recipe.JacksonParserRecipe;
import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.fe.schema.XSDDatatype;

/* PoC Streaming Parser to Match Groovy Parser in Java 7 */
public class Inspire_TN_RoadLinkParserRecipe extends JacksonParserRecipe {

    @Override
    public void parse() throws IOException {

        getGeometryDeserializer().mapGeometryTypes(
                "http://www.opengis.net/gml/3.2", "LineString", "Curve",
                "CompositeCurve", "OrientableCurve", "MultiCurve");

        FeatureOutputContext outputContext = new FeatureOutputContext(
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

        OutputFeature<RoadLink> outputFeature = new OutputFeature<RoadLink>(
                outputContext);

        InputFeature<RoadLink> iter = new InputFeature<RoadLink>(RoadLink.QN,
                RoadLink.class);

        while (iter.hasNext()) {
            RoadLink roadLink = iter.next();
            Resource output_ID = outputContext.uniqueId(roadLink.id);

            outputFeature.setFeature(roadLink).
                setId(output_ID);

            outputFeature.addGeometryProperty(geom,
                    roadLink.centrelineGeometry.geometry);

            outputFeature
                    .addProperty(gn, roadLink.geographicalName)
                    .addProperty(validFrom, roadLink.validFrom)
                    .addProperty(validTo, roadLink.validTo)
                    .addProperty(fictitious, roadLink.fictitious)
                    .addProperty(beginLifespanVersion,
                            roadLink.beginLifespanVersion)
                    .addProperty(inspireId, roadLink.inspireId)
                    .addProperty(endLifespanVersion,
                            roadLink.endLifespanVersion);

            outputFeature.build();

        }

    }

}