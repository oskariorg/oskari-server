package fi.nls.oskari.eu.elf.recipe.geographicalnames;

import java.io.IOException;

import fi.nls.oskari.eu.elf.geographicalnames.ELF_MasterLoD1_NamedPlace.NamedPlace;
import fi.nls.oskari.fe.input.format.gml.recipe.JacksonParserRecipe.GML32;
import fi.nls.oskari.fe.iri.Resource;

public class ELF_MasterLoD1_NamedPlace_Parser extends GML32 {

    @Override
    public void parse() throws IOException {

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
            final NamedPlace namedPlace = iter.next();
            final Resource output_ID = outputContext.uniqueId(namedPlace.id);

            outputFeature.setFeature(namedPlace).setId(output_ID);

            outputFeature.addGeometryProperty(geom,
                    namedPlace.geometry.geometry);

            outputFeature
                    .addProperty(gn, namedPlace.name)
                    .addProperty(beginLifespanVersion,
                            namedPlace.beginLifespanVersion)
                    .addProperty(inspireId, namedPlace.inspireId)
                    .addProperty(endLifespanVersion,
                            namedPlace.endLifespanVersion);

            outputFeature.addProperty(obj, namedPlace);

            outputFeature.build();

        }

    }

}
