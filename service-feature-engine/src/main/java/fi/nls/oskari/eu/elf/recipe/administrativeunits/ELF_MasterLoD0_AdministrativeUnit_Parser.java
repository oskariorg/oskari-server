package fi.nls.oskari.eu.elf.recipe.administrativeunits;

import fi.nls.oskari.eu.elf.administrativeunits.ELF_MasterLoD1_AdministrativeUnit.AdministrativeUnit;
import fi.nls.oskari.fe.input.format.gml.recipe.JacksonParserRecipe.GML32;
import fi.nls.oskari.fe.iri.Resource;

import java.io.IOException;

public class ELF_MasterLoD0_AdministrativeUnit_Parser extends GML32 {

    @Override
    public void parse() throws IOException {

        setLenient(true);

        
        final FeatureOutputContext outputContext = new FeatureOutputContext(
                AdministrativeUnit.QN);

        outputContext.addDefaultGeometryProperty();
        final Resource beginLifespanVersion = outputContext
                .addOutputStringProperty("beginLifespanVersion");
        final Resource inspireId = outputContext.addOutputProperty("inspireId");
        final Resource endLifespanVersion = outputContext
                .addOutputStringProperty("endLifespanVersion");
        final Resource obj = outputContext.addOutputStringProperty("obj");

        outputContext.build();

        final OutputFeature<AdministrativeUnit> outputFeature = new OutputFeature<AdministrativeUnit>(
                outputContext);

        final InputFeature<AdministrativeUnit> iter = new InputFeature<AdministrativeUnit>(
                AdministrativeUnit.QN, AdministrativeUnit.class);

        while (iter.hasNext()) {
            final AdministrativeUnit feature = iter.next();
            final Resource output_ID = outputContext.uniqueId(feature.id);

            outputFeature.setFeature(feature).setId(output_ID);

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
