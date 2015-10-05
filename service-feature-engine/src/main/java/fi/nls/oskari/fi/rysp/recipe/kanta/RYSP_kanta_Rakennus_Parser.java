package fi.nls.oskari.fi.rysp.recipe.kanta;

import fi.nls.oskari.fe.input.format.gml.recipe.JacksonParserRecipe.GML31;
import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.fi.rysp.kantakartta.RYSP_kanta_Rakennus.Rakennus;
import fi.nls.oskari.fi.rysp.kantakartta.RYSP_kanta_Rakennus.Sijainti;

import java.io.IOException;

public class RYSP_kanta_Rakennus_Parser extends GML31 {

    @Override
    public void parse() throws IOException {
        setLenient(true);
        final FeatureOutputContext outputContext = new FeatureOutputContext(
                Rakennus.QN);

        final Resource geom = outputContext.addDefaultGeometryProperty();
        final Resource gn = outputContext.addOutputProperty("name");
        final Resource beginLifespanVersion = outputContext
                .addOutputStringProperty("beginLifespanVersion");
        outputContext.addOutputProperty("inspireId");
        final Resource endLifespanVersion = outputContext
                .addOutputStringProperty("endLifespanVersion");
        final Resource obj = outputContext.addOutputStringProperty("obj");

        outputContext.build();

        final OutputFeature<Rakennus> outputFeature = new OutputFeature<Rakennus>(
                outputContext);

        final InputFeature<Rakennus> iter = new InputFeature<Rakennus>(
                Rakennus.QN, Rakennus.class);

        while (iter.hasNext()) {
            final Rakennus feature = iter.next();

            if (!(feature.sijainnit != null
                    && feature.sijainnit.Sijainti != null && !feature.sijainnit.Sijainti
                        .isEmpty())) {
                continue;
            }

            // there may be unbounded Sijaint with a choice of geometry
            for (Sijainti s : feature.sijainnit.Sijainti) {

                final Resource output_ID = outputContext.uniqueId(feature.id);
                outputFeature.setFeature(feature).setId(output_ID);

                // referenssipiste has some issues ATM
                if (s.alue != null) {
                    outputFeature.addGeometryProperty(geom,
                            s.alue.getGeometry());
                } else if (s.keskilinja != null) {
                    outputFeature.addGeometryProperty(geom,
                            s.keskilinja.getGeometry());

                } else if (s.reunaviiva != null) {
                    outputFeature.addGeometryProperty(geom,
                            s.reunaviiva.getGeometry());
                }

                outputFeature.addProperty(gn, feature.id)
                        .addProperty(beginLifespanVersion, feature.alkuPvm)
                        .addProperty(endLifespanVersion, feature.loppuPvm);

                outputFeature.addProperty(obj, feature);

                outputFeature.build();
            }

        }

    }

}
