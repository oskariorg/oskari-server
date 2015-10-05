package fi.nls.oskari.eu.elf.recipe.addresses;

import fi.nls.oskari.eu.elf.addresses.ELF_MasterLoD0_Address.Address;
import fi.nls.oskari.fe.input.format.gml.recipe.JacksonParserRecipe.GML32;
import fi.nls.oskari.fe.iri.Resource;

import java.io.IOException;

public class ELF_MasterLoD0_Address_nls_fi_wfs_Parser extends GML32 {

    @Override
    public void parse() throws IOException {

        setLenient(true);
        
        final FeatureOutputContext outputContext = new FeatureOutputContext(
                Address.QN);

        final Resource geom = outputContext.addDefaultGeometryProperty();
        final Resource beginLifespanVersion = outputContext
                .addOutputStringProperty("beginLifespanVersion");
        final Resource inspireId = outputContext.addOutputProperty("inspireId");
        final Resource endLifespanVersion = outputContext
                .addOutputStringProperty("endLifespanVersion");
        final Resource obj = outputContext.addOutputStringProperty("obj");

        outputContext.build();

        final OutputFeature<Address> outputFeature = new OutputFeature<Address>(
                outputContext);

        final InputFeature<Address> iter = new InputFeature<Address>(
                Address.QN, Address.class);

        while (iter.hasNext()) {
            final Address feature = iter.next();
            final Resource output_ID = outputContext.uniqueId(feature.id);

            outputFeature.setFeature(feature).setId(output_ID);

            if (feature.position.get(0) != null
                    && feature.position.get(0).GeographicPosition != null
                    && feature.position.get(0).GeographicPosition.geometry != null) {
                outputFeature
                        .addGeometryProperty(
                                geom,
                                feature.position.get(0).GeographicPosition.geometry.getGeometry());
            }

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
