package fi.nls.oskari.printout.ws.jaxrs.format;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.filter.request.RequestFilterException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

import fi.nls.oskari.printout.imaging.ScaleOps;
import fi.nls.oskari.printout.input.layers.LayerDefinition;
import fi.nls.oskari.printout.input.maplink.MapLink;
import fi.nls.oskari.printout.output.layer.AsyncLayerProcessor;
import fi.nls.oskari.printout.output.map.MapProducer;
import fi.nls.oskari.printout.output.map.MetricScaleResolutionUtils;

/**
 * support for M$ format documents - this does not work atm
 */
public class StreamingDOCXImpl implements StreamingOutput {

	private MapLink mapLink;
	private MapProducer producer;

	ScaleOps scaleOps = new ScaleOps();
	private BufferedImage image;

	public StreamingDOCXImpl(MapProducer producer, MapLink mapLink) {
		this.producer = producer;
		this.mapLink = mapLink;
	}

	
	public void write(OutputStream outs) throws IOException,
			WebApplicationException {

		XWPFDocument doc = new XWPFDocument();

		XWPFParagraph p1 = doc.createParagraph();
		p1.setAlignment(ParagraphAlignment.CENTER);

		XWPFRun r1 = p1.createRun();

		int width = mapLink.getWidth();
		int height = mapLink.getHeight();

		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		ImageIO.write(image, "png", buf);
		buf.close();

		byte[] bytes = buf.toByteArray();

		try {
			r1.setText("png");
			r1.addBreak();

			r1.addPicture(new ByteArrayInputStream(bytes),
					Document.PICTURE_TYPE_PNG, "pic.png", Units.toEMU(width),
					Units.toEMU(height));

		} catch (InvalidFormatException e) {
			throw new IOException(e);
		}
		// r1.addBreak(BreakType.PAGE);

		doc.write(outs);

	}

	public void underflow() throws ParseException, IOException,
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
