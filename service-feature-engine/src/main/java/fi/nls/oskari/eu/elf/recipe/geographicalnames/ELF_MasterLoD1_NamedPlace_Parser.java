package fi.nls.oskari.eu.elf.recipe.geographicalnames;

import fi.nls.oskari.eu.elf.geographicalnames.ELF_MasterLoD1_NamedPlace.NamedPlace;
import fi.nls.oskari.fe.input.format.gml.recipe.JacksonParserRecipe.GML32;
import fi.nls.oskari.fe.iri.Resource;

import java.io.IOException;

public class ELF_MasterLoD1_NamedPlace_Parser extends GML32 {

    @Override
    public void parse() throws IOException {

        setLenient(true);

        final FeatureOutputContext outputContext = new FeatureOutputContext(
                NamedPlace.QN);

        final Resource geom = outputContext.addDefaultGeometryProperty();
        final Resource gn = outputContext.addOutputProperty("name");
        final Resource beginLifespanVersion = outputContext
                .addOutputStringProperty("beginLifespanVersion");
        final Resource inspireId = outputContext.addOutputProperty("inspireId");
        final Resource endLifespanVersion = outputContext
                .addOutputStringProperty("endLifespanVersion");

        final Resource obj = outputContext.addOutputStringProperty("obj");

        outputContext.build();

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
                    .addProperty(endLifespanVersion,
                            feature.endLifespanVersion);

            if(feature.inspireId != null) outputFeature.addProperty(inspireId, feature.inspireId);

            outputFeature.addProperty(obj, feature);

            outputFeature.build();

        }

    }

}
