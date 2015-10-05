package fi.nls.oskari.eu.inspire.recipe.geographicalnames;

import fi.nls.oskari.eu.inspire.geographicalnames.INSPIRE_gn_NamedPlace.NamedPlace;
import fi.nls.oskari.fe.input.format.gml.recipe.JacksonParserRecipe.GML32;
import fi.nls.oskari.fe.iri.Resource;

import java.io.IOException;

//import fi.nls.oskari.eu.inspire.gmlas.geographicalnames.NamedPlace;

public class INSPIRE_GN_NamedPlace extends GML32 {

    @Override
    public void parse() throws IOException {

        setLenient(true);
        // getGeometryDeserializer().setIgnoreProps(true);

        final FeatureOutputContext outputContext = new FeatureOutputContext(
                NamedPlace.QN);

        final Resource geom = outputContext.addDefaultGeometryProperty();
        final Resource gn = outputContext.addOutputProperty("name");
        final Resource beginLifespanVersion = outputContext
                .addOutputStringProperty("beginLifespanVersion");
        final Resource inspireId = outputContext.addOutputProperty("inspireId");
        final Resource endLifespanVersion = outputContext
                .addOutputStringProperty("endLifespanVersion");

        final OutputFeature<NamedPlace> outputFeature = new OutputFeature<NamedPlace>(
                outputContext);

        final InputFeature<NamedPlace> iter = new InputFeature<NamedPlace>(
                NamedPlace.QN, NamedPlace.class);

        while (iter.hasNext()) {
            final NamedPlace feature = iter.next();
            final Resource output_ID = outputContext.uniqueId(feature.id);

            outputFeature.setFeature(feature).setId(output_ID);

            if (feature.geometry != null) {
                outputFeature.addGeometryProperty(geom,
                        feature.geometry.getGeometry());
            }

            outputFeature
                    .addProperty(gn, feature.name)
                    .addProperty(beginLifespanVersion,
                            feature.beginLifespanVersion)
                    .addProperty(inspireId, feature.inspireId)
                    .addProperty(endLifespanVersion, feature.endLifespanVersion);

            outputFeature.build();

        }

    }

}
