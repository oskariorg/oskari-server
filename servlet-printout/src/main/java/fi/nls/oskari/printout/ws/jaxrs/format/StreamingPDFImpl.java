package fi.nls.oskari.printout.ws.jaxrs.format;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;
import fi.nls.oskari.printout.imaging.ScaleOps;
import fi.nls.oskari.printout.input.layers.LayerDefinition;
import fi.nls.oskari.printout.input.maplink.MapLink;
import fi.nls.oskari.printout.output.layer.AsyncLayerProcessor;
import fi.nls.oskari.printout.output.map.MapProducer;
import fi.nls.oskari.printout.printing.PDFProducer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.filter.request.RequestFilterException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Outputs (and creates) PDF document as a result from JAX-RS request.
 * 
 */
public class StreamingPDFImpl implements StreamingOutput {
	private static Log log = LogFactory.getLog(StreamingPDFImpl.class);
	final MapProducer producer;
	final MapLink mapLink;
	final PDFProducer.Options pageOptions;
	final ScaleOps scaleOps = new ScaleOps();

	final ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();

	final PDFProducer.Page page;
	final private Envelope env;
	final private Point centre;
	final private int width;
	final private int height;

	/**
	 * 
	 * @param producer
	 * @param mapLink
	 * @param pageOptions
	 * @throws NoSuchAuthorityCodeException
	 * @throws IOException
	 * @throws GeoWebCacheException
	 * @throws FactoryException
	 */
	public StreamingPDFImpl(MapProducer producer, MapLink mapLink,
			PDFProducer.Page page, PDFProducer.Options pageOptions)
			throws NoSuchAuthorityCodeException, IOException,
			GeoWebCacheException, FactoryException {
		this.page = page;
		this.mapLink = mapLink;
		this.producer = producer;
		this.pageOptions = pageOptions;

		width = page.getMapWidthTargetInPoints(pageOptions);
		height = page.getMapHeightTargetInPoints(pageOptions);
		centre = mapLink.getCentre();
		env = producer.getProcessor().getEnvFromPointZoomAndExtent(centre,
				mapLink.getZoom(), width, height);

	}

	/**
	 * 
	 * @param page
	 * @throws IOException
	 * @throws ParseException
	 * @throws GeoWebCacheException
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 * @throws RequestFilterException
	 * @throws TransformException
	 * @throws InterruptedException
	 * @throws URISyntaxException
	 */
	public void underflow() throws IOException, ParseException,
			GeoWebCacheException, XMLStreamException,
			FactoryConfigurationError, RequestFilterException,
			TransformException, InterruptedException, URISyntaxException {

		AsyncLayerProcessor asyncProc = new AsyncLayerProcessor();
		asyncProc.start();
		try {

			for (LayerDefinition ldef : mapLink.getMapLinkLayers()) {

				LayerDefinition inScale = mapLink
						.selectLayerDefinitionForScale(ldef);
				if (inScale == null) {
					continue;
				}

				final List<LayerDefinition> overlayLayers = new ArrayList<LayerDefinition>(
						1);

				overlayLayers.add(inScale);
				BufferedImage image = producer.getMap(asyncProc, env,
						mapLink.getZoom(), width, height, overlayLayers,
						MapProducer.ImageType.ARGB, null);

				if (image != null) {

					BufferedImage scaledImage = scaleOps.doScaleWithFilters(
							image, width * 2, height * 2);

					images.add(scaledImage);

					image.flush();
				}

			}
		} finally {
			asyncProc.shutdown();
		}

	}

	/*
	 * public void underflow() throws IOException, ParseException,
	 * GeoWebCacheException, XMLStreamException, FactoryConfigurationError,
	 * RequestFilterException, TransformException {
	 * 
	 * Point centre = mapLink.getCentre(); int width = mapLink.getWidth(); int
	 * height = mapLink.getHeight();
	 * 
	 * Envelope env = null;
	 * 
	 * if (mapLink.getValues().get("BBOX") != null) { String[] bboxparts =
	 * mapLink.getValues().get("BBOX").split(","); env = new
	 * Envelope(Double.valueOf(bboxparts[0]), Double.valueOf(bboxparts[2]),
	 * Double.valueOf(bboxparts[1]), Double.valueOf(bboxparts[3]));
	 * 
	 * } else { env =
	 * producer.getProcessor().getEnvFromPointZoomAndExtent(centre,
	 * mapLink.getZoom(), width, height); }
	 * 
	 * AsyncLayerProcessor asyncProc = new AsyncLayerProcessor();
	 * asyncProc.start(); try {
	 * 
	 * for (LayerDefinition ldef : mapLink.getMapLinkLayers()) {
	 * 
	 * LayerDefinition inScale = ldef.selectLayerDefinitionForScale(); if
	 * (inScale == null) { continue; }
	 * 
	 * final List<LayerDefinition> overlayLayers = new
	 * ArrayList<LayerDefinition>( 1);
	 * 
	 * overlayLayers.add(inScale); BufferedImage image;
	 * 
	 * image = producer.getMap(asyncProc, env, mapLink.getZoom(), width, height,
	 * overlayLayers, MapProducer.ImageType.ARGB);
	 * 
	 * 
	 * BufferedImage scaledImage = doScaleWithFilters(image, width * 2, height *
	 * 2);
	 * 
	 * images.add(scaledImage);
	 * 
	 * image.flush(); } } finally {
	 * 
	 * try { asyncProc.shutdown(); } catch (InterruptedException e) { throw new
	 * IOException(e); }
	 * 
	 * }
	 * 
	 * }
	 */

	/**
	 * 
	 * @param page
	 * @throws IOException
	 * @throws ParseException
	 * @throws GeoWebCacheException
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 * @throws RequestFilterException
	 * @throws TransformException
	 * @throws InterruptedException
	 * @throws URISyntaxException
	 */
	public void underflowWithAdjust() throws IOException, ParseException,
			GeoWebCacheException, XMLStreamException,
			FactoryConfigurationError, RequestFilterException,
			TransformException, InterruptedException, URISyntaxException {

		double widthInches = width / 72.0;
		double widthCm = widthInches * 2.54;
		double scaleInMeters = mapLink.getScale() > 5000 ? Math.floor(mapLink
				.getScale() / 5000) * 5000 : mapLink.getScale();
		double widthInMeters = widthCm * scaleInMeters / 100;

		double heightInches = height / 72.0;
		double heightCm = heightInches * 2.54;
		double heightInMeters = heightCm * scaleInMeters / 100;

		Envelope envAdj = new Envelope(centre.getX() - widthInMeters / 2.0,
				centre.getX() + widthInMeters / 2.0, centre.getY()
						- heightInMeters / 2.0, centre.getY() + heightInMeters
						/ 2.0);

		double adjX = env.getWidth() / envAdj.getWidth();
		double adjY = env.getHeight() / envAdj.getHeight();

		int adjWidth = Double.valueOf(adjX * width).intValue();
		int adjHeight = Double.valueOf(adjY * height).intValue();

		Envelope envReq = producer.getProcessor().getEnvFromPointZoomAndExtent(
				centre, mapLink.getZoom(), adjWidth, adjHeight);

		log.info("cm for " + width + "points " + widthCm + "," + heightCm);

		log.info("SCALE " + mapLink.getScale());
		log.info("WIDTH  in METERS" + env.getWidth());
		log.info("WIDTHAdj in METERS" + envAdj.getWidth());
		log.info("SCALE RESULT % " + adjX + "," + adjY);
		log.info("SCALE RESULT px " + adjX * width + "," + adjY * width);
		log.info("BBOX " + env);
		log.info("BBOXAdj " + envAdj);
		log.info("Adj WxH " + adjWidth + "x" + adjHeight);
		log.info("BBOXReq " + envReq);
		log.info("-----------");

		AsyncLayerProcessor asyncProc = new AsyncLayerProcessor();
		asyncProc.start();
		try {

			for (LayerDefinition ldef : mapLink.getMapLinkLayers()) {

				LayerDefinition inScale = mapLink
						.selectLayerDefinitionForScale(ldef);
				if (inScale == null) {
					continue;
				}

				final List<LayerDefinition> overlayLayers = new ArrayList<LayerDefinition>(
						1);

				overlayLayers.add(inScale);
				BufferedImage image = producer.getMap(asyncProc, envReq,
						mapLink.getZoom(), adjWidth, adjHeight, overlayLayers,
						MapProducer.ImageType.ARGB, envAdj);

				if (image != null) {
					BufferedImage scaledImage = scaleOps.doScaleWithFilters(
							image, width * 2, height * 2);

					images.add(scaledImage);

					image.flush();
				}

			}
		} finally {
			asyncProc.shutdown();
		}

	}

	/**
     * 
     */

	public void write(OutputStream outputStream) throws IOException,
			WebApplicationException {

		PDFProducer pdf = new PDFProducer(page, pageOptions, producer.getCrs());

		try {
			/*
			 * int width = mapLink.getWidth(); int height = mapLink.getHeight();
			 */

			pdf.createLayeredPDFFromImages(images, outputStream, env, centre);

		} catch (COSVisitorException e) {

			throw new IOException(e);
		} catch (TransformException e) {
			throw new IOException(e);
		} catch (Exception e) {
			throw new IOException(e);
		}

		for (BufferedImage image : images) {
			image.flush();

		}
	}

}
