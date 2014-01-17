package fi.nls.oskari.printout.ws.jaxrs.format;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
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

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
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

/**
 *  Outputs and creates a PPTX slideshow with an embedded map image as a result for 
 *  	JAX-RS map image request.  
 */
public class StreamingPPTXImpl implements StreamingOutput {
	ScaleOps scaleOps = new ScaleOps();
	private MapProducer producer;
	private MapLink mapLink;
	private BufferedImage image;

	public StreamingPPTXImpl(MapProducer producer, MapLink mapLink) {
		this.producer = producer;
		this.mapLink = mapLink;
	}

	
	public void write(OutputStream outs) throws IOException,
			WebApplicationException {
		XMLSlideShow ppt = new XMLSlideShow();

		XSLFSlide slide = ppt.createSlide();
		/*
		 * XSLFTextBox shape = slide.createTextBox(); shape.setAnchor(new
		 * Rectangle(50, 50, 400, 200));
		 * 
		 * XSLFTextParagraph p1 = shape.addNewTextParagraph(); p1.setLevel(0);
		 * p1.setBullet(true); XSLFTextRun r1 = p1.addNewTextRun();
		 * r1.setText("Bullet1");
		 * 
		 * XSLFTextParagraph p2 = shape.addNewTextParagraph(); // indentation
		 * before text p2.setLeftMargin(60); // the bullet is set 40 pt before
		 * the text p2.setIndent(-40); p2.setBullet(true); // customize bullets
		 * p2.setBulletFontColor(Color.red); p2.setBulletFont("Wingdings");
		 * p2.setBulletCharacter("\u0075"); p2.setLevel(1); XSLFTextRun r2 =
		 * p2.addNewTextRun(); r2.setText("Bullet2");
		 * 
		 * // the next three paragraphs form an auto-numbered list
		 * XSLFTextParagraph p3 = shape.addNewTextParagraph();
		 * p3.setBulletAutoNumber(ListAutoNumber.ALPHA_LC_PARENT_R, 1);
		 * p3.setLevel(2); XSLFTextRun r3 = p3.addNewTextRun();
		 * r3.setText("Numbered List Item - 1");
		 * 
		 * XSLFTextParagraph p4 = shape.addNewTextParagraph();
		 * p4.setBulletAutoNumber(ListAutoNumber.ALPHA_LC_PARENT_R, 2);
		 * p4.setLevel(2); XSLFTextRun r4 = p4.addNewTextRun();
		 * r4.setText("Numbered List Item - 2");
		 * 
		 * XSLFTextParagraph p5 = shape.addNewTextParagraph();
		 * p5.setBulletAutoNumber(ListAutoNumber.ALPHA_LC_PARENT_R, 3);
		 * p5.setLevel(2); XSLFTextRun r5 = p5.addNewTextRun();
		 * r5.setText("Numbered List Item - 3");
		 * 
		 * shape.resizeToFitText();
		 */
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		ImageIO.write(image, "png", buf);
		buf.close();

		byte[] data = buf.toByteArray();
		int pictureIndex = ppt.addPicture(data,
				XSLFPictureData.PICTURE_TYPE_PNG);

		XSLFPictureShape shape = slide.createPicture(pictureIndex);
		shape.setAnchor(new Rectangle(5, 5, mapLink.getWidth(), mapLink
				.getHeight()));

		ppt.write(outs);

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
