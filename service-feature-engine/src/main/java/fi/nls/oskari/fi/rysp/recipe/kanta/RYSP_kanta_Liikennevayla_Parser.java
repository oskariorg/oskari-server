package fi.nls.oskari.fi.rysp.recipe.kanta;

import java.io.IOException;

import fi.nls.oskari.fe.input.format.gml.recipe.JacksonParserRecipe.GML31;
import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.fi.rysp.kantakartta.RYSP_kanta_Liikennevayla.Liikennevayla;

public class RYSP_kanta_Liikennevayla_Parser extends GML31 {

    @Override
    public void parse() throws IOException {
       // setLenient(true);

        getGeometryDeserializer().mapGeometryTypes(
                "http://www.opengis.net/gml", "Curve", "LineString",
                "LinearRing",
                "MultiLineString",
                "MultiCurve",
                "Point", "MultiPoint");

        FeatureOutputContext outputContext = new FeatureOutputContext(
                Liikennevayla.QN);

        final Resource geom = outputContext.addDefaultGeometryProperty();
        final Resource gn = outputContext.addOutputProperty("name");
        final Resource beginLifespanVersion = outputContext
                .addOutputStringProperty("beginLifespanVersion");
        final Resource inspireId = outputContext.addOutputProperty("inspireId");
        final Resource endLifespanVersion = outputContext
                .addOutputStringProperty("endLifespanVersion");
        final Resource obj = outputContext
                .addOutputStringProperty("obj");

        outputContext.build();

        OutputFeature<Liikennevayla> outputFeature = new OutputFeature<Liikennevayla>(
                outputContext);

        InputFeature<Liikennevayla> iter = new InputFeature<Liikennevayla>(
                Liikennevayla.QN, Liikennevayla.class);

        while (iter.hasNext()) {
            Liikennevayla feature = iter.next();
            Resource output_ID = outputContext.uniqueId(feature.id);

            outputFeature.setFeature(feature).setId(output_ID);

           /* if (feature.sijainnit != null && feature.sijainnit.Sijainti != null) {
                if (feature.sijainnit.Sijainti.alue != null) {
                    outputFeature.addGeometryProperty(geom,
                            feature.sijainnit.Sijainti.alue.geometry);
                } else if (feature.sijainnit.Sijainti.keskilinja != null) {
                    outputFeature.addGeometryProperty(geom,
                            feature.sijainnit.Sijainti.keskilinja.geometry);

                } else if (feature.sijainnit.Sijainti.reunaviiva != null) {
                    outputFeature.addGeometryProperty(geom,
                            feature.sijainnit.Sijainti.reunaviiva.geometry);
                }
            }
            */
            

            outputFeature.addProperty(gn, feature.id)
                    .addProperty(beginLifespanVersion, feature.alkuPvm)
                    .addProperty(endLifespanVersion, feature.loppuPvm);
            
            outputFeature.addProperty(obj,feature);

            outputFeature.build();

        }

    }

}
