package fi.nls.oskari.eu.elf.recipe.tn;

import java.io.IOException;

import fi.nls.oskari.eu.elf.roadtransportnetwork.ELF_TNRO_RoadLink.RoadLink;
import fi.nls.oskari.fe.input.format.gml.recipe.JacksonParserRecipe.GML32;
import fi.nls.oskari.fe.iri.Resource;

public class ELF_MasterLoD1_RoadLink_Parser extends GML32 {

    @Override
    public void parse() throws IOException {

        getGeometryDeserializer().mapGeometryTypes(
                "http://www.opengis.net/gml/3.2", "Polygon", "Surface",
                "PolyhedralSurface", "TriangulatedSurface", "Tin",
                "OrientableSurface", "CompositeSurface", "LineString", "Curve",
                "CompositeCurve", "OrientableCurve", "MultiCurve", "Point",
                "MultiPoint");

        FeatureOutputContext outputContext = new FeatureOutputContext(
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

        OutputFeature<RoadLink> outputFeature = new OutputFeature<RoadLink>(
                outputContext);

        InputFeature<RoadLink> iter = new InputFeature<RoadLink>(RoadLink.QN,
                RoadLink.class);

        while (iter.hasNext()) {
            RoadLink feature = iter.next();
            Resource output_ID = outputContext.uniqueId(feature.id);

            outputFeature.setFeature(feature).setId(output_ID);

            if (feature.centrelineGeometry != null) {
                outputFeature.addGeometryProperty(geom,
                        feature.centrelineGeometry.geometry);
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
