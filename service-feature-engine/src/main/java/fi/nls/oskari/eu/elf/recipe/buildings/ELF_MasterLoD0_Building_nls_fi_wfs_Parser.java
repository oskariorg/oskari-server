package fi.nls.oskari.eu.elf.recipe.buildings;

import fi.nls.oskari.eu.elf.buildings.ELF_MasterLoD0_Building.Building;
import fi.nls.oskari.fe.input.format.gml.recipe.JacksonParserRecipe.GML32;
import fi.nls.oskari.fe.iri.Resource;

import java.io.IOException;

public class ELF_MasterLoD0_Building_nls_fi_wfs_Parser extends GML32 {

    @Override
    public void parse() throws IOException {

        setLenient(true);
        
        final FeatureOutputContext outputContext = new FeatureOutputContext(
                Building.QN);

        final Resource geom = outputContext.addDefaultGeometryProperty();
        final Resource gn = outputContext.addOutputProperty("name");
        final Resource beginLifespanVersion = outputContext
                .addOutputStringProperty("beginLifespanVersion");
        final Resource inspireId = outputContext.addOutputProperty("inspireId");
        final Resource endLifespanVersion = outputContext
                .addOutputStringProperty("endLifespanVersion");
        final Resource obj = outputContext.addOutputStringProperty("obj");

        outputContext.build();

        final OutputFeature<Building> outputFeature = new OutputFeature<Building>(
                outputContext);

        final InputFeature<Building> iter = new InputFeature<Building>(
                Building.QN, Building.class);
        // Iterate features
        while (iter.hasNext()) {
            final Building feature = iter.next();
            final Resource output_ID = outputContext.uniqueId(feature.id);

            outputFeature.setFeature(feature).setId(output_ID);

            if (feature.geometry2D != null
                    && feature.geometry2D.BuildingGeometry2D != null
                    && feature.geometry2D.BuildingGeometry2D.geometry != null) {
                outputFeature
                        .addGeometryProperty(
                                geom,
                                feature.geometry2D.BuildingGeometry2D.geometry.getGeometry());
            }

            outputFeature
                    .addProperty(gn, feature.name)
                    .addProperty(beginLifespanVersion,
                            feature.beginLifespanVersion)
                    .addProperty(inspireId, feature.inspireId)
                    .addProperty(endLifespanVersion, feature.endLifespanVersion)
                    .addProperty(obj, feature);

            outputFeature.build();

        }

    }

}
