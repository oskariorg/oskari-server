package fi.nls.oskari.printout.ws.jaxrs.format;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.filter.request.RequestFilterException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

import fi.nls.oskari.printout.imaging.ScaleOps;
import fi.nls.oskari.printout.input.layers.LayerDefinition;
import fi.nls.oskari.printout.input.maplink.MapLink;
import fi.nls.oskari.printout.output.layer.AsyncLayerProcessor;
import fi.nls.oskari.printout.output.map.MapProducer;

/**
 *  Outputs and creates a PNG image as a result for JAX-RS map image request.
 */
public class StreamingPNGImpl implements StreamingOutput {
	private static Log log = LogFactory.getLog(StreamingPNGImpl.class);

	MapProducer producer;
	MapLink mapLink;
	BufferedImage image;
	ScaleOps scaleOps = new ScaleOps();

	public StreamingPNGImpl(MapProducer producer, MapLink mapLink)
			throws NoSuchAuthorityCodeException, IOException,
			GeoWebCacheException, FactoryException {
		this.mapLink = mapLink;
		this.producer = producer;
		this.image = null;

	}

	
	public void write(OutputStream outputStream) throws IOException,
			WebApplicationException {

		try {
			log.info("WRITING PNG ======================  ");
			ImageIO.write(image, "png", outputStream);
		} finally {
			image.flush();
		}

	}

	public void underflow() throws IOException, ParseException,
			GeoWebCacheException, XMLStreamException,
			FactoryConfigurationError, RequestFilterException,
			TransformException, URISyntaxException {

		final List<LayerDefinition> selectedLayers = new ArrayList<LayerDefinition>();

		for (LayerDefinition ldef : mapLink.getMapLinkLayers()) {
			LayerDefinition inScale = mapLink
					.selectLayerDefinitionForScale(ldef);
			if (inScale == null) {
				continue;
			}

			selectedLayers.add(inScale);

		}

		Point centre = mapLink.getCentre();
		int zoom = mapLink.getZoom();

		int width = mapLink.getWidth();
		int height = mapLink.getHeight();

		Envelope env = producer.getProcessor().getEnvFromPointZoomAndExtent(
				centre, zoom, width, height);

		AsyncLayerProcessor asyncProc = new AsyncLayerProcessor();
		asyncProc.start();
		try {

			BufferedImage producedImage = producer.getMap(asyncProc, env, zoom,
					width, height, selectedLayers, MapProducer.ImageType.ARGB);

			String scaledWidth = mapLink.getValues().get("SCALEDWIDTH");
			String scaledHeight = mapLink.getValues().get("SCALEDHEIGHT");

			if (scaledWidth != null && scaledHeight != null) {
				image = scaleOps.doScaleWithFilters(producedImage,
						Integer.valueOf(scaledWidth, 10),
						Integer.valueOf(scaledHeight, 10));
			} else {
				image = producedImage;
			}
		} finally {
			try {
				asyncProc.shutdown();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				throw new IOException(e);
			}
		}

	}

}
