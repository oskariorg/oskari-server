package fi.nls.oskari.eu.elf.recipe.administrativeunits;

import fi.nls.oskari.eu.elf.administrativeunits.ELF_MasterLoD1_AdministrativeBoundary.AdministrativeBoundary;
import fi.nls.oskari.fe.input.format.gml.recipe.JacksonParserRecipe.GML32;
import fi.nls.oskari.fe.iri.Resource;

import java.io.IOException;

public class ELF_MasterLoD0_AdministrativeBoundary_nls_fi_wfs_Parser extends GML32 {

    @Override
    public void parse() throws IOException {

        setLenient(true);

        final FeatureOutputContext outputContext = new FeatureOutputContext(
                AdministrativeBoundary.QN);

        final Resource geom = outputContext.addDefaultGeometryProperty();
        final Resource beginLifespanVersion = outputContext
                .addOutputStringProperty("beginLifespanVersion");
        final Resource inspireId = outputContext.addOutputProperty("inspireId");
        final Resource endLifespanVersion = outputContext
                .addOutputStringProperty("endLifespanVersion");
        final Resource obj = outputContext.addOutputStringProperty("obj");

        outputContext.build();

        final OutputFeature<AdministrativeBoundary> outputFeature = new OutputFeature<AdministrativeBoundary>(
                outputContext);

        final InputFeature<AdministrativeBoundary> iter = new InputFeature<AdministrativeBoundary>(
                AdministrativeBoundary.QN, AdministrativeBoundary.class);

        while (iter.hasNext()) {
            final AdministrativeBoundary feature = iter.next();
            final Resource output_ID = outputContext.uniqueId(feature.id);

            outputFeature.setFeature(feature).setId(output_ID);

            if (feature.geometry != null)
                outputFeature
                        .addGeometryProperty(
                                geom,
                                feature.geometry.getGeometry());

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
