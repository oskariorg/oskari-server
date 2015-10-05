package fi.nls.oskari.printout.ws.jaxrs.format;

import org.geotools.feature.FeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;

/**
 * writes out featurecollection as a JAX-RS result
 *
 */
public class StreamingJSONImpl implements StreamingOutput {

	private FeatureCollection<SimpleFeatureType, SimpleFeature> fc;

	public StreamingJSONImpl(
			FeatureCollection<SimpleFeatureType, SimpleFeature> fc) {
		this.fc = fc;
	}

	
	public void write(OutputStream outs) throws IOException,
			WebApplicationException {

		FeatureJSON fjson = new FeatureJSON();
		try {
			fjson.writeFeatureCollection(fc, outs);
		} finally {

		}
	}

}
