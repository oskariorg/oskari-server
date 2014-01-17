package fi.nls.oskari.printout.ws.jaxrs.resource;

import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.filter.request.RequestFilterException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

import fi.nls.oskari.printout.config.ConfigValue;
import fi.nls.oskari.printout.ws.jaxrs.format.StreamingJSONImpl;
import fi.nls.oskari.printout.ws.jaxrs.map.WebServiceMapProducerResource;

/**
 * 
 * JAX-RS API for printout Methods of this class provide access either via GET
 * URL parameter based requests or POST JSON requests.
 * 
 * API paths are combined based on @Path annotations from below.
 * 
 * see. https://jersey.java.net/documentation/latest/user-guide.html
 * https://jersey.java.net/documentation/1.7/user-guide.html
 * 
 * 
 * @todo fix parameter handling
 * 
 */
@Path("/imaging")
public class MapResource {
	enum MapLinkArg {
		/**
		 * maplink argument used in scale calculations
		 */
		zoomLevel,
		/**
		 * maplink argument used in location calculations
		 */
		coord,
		/**
         * 
         * 
         */
		mapLayers,
		/**
         * 
         * 
         */
		width,
		/**
          * 
          * 
          */
		height,
		/**
          * 
          * 
          */
		scaledWidth,
		/**
         * 
         * 
         */
		scaledHeight,
		/**
          * 
          * 
          */
		bbox,
		/**
          * 
          * 
          */
		pageSize,
		/**
          * 
          * 
          */
		pageTitle,
		/**
          * 
          * 
          */
		pageLogo,
		/**
          * 
          * 
          */
		pageDate,
		/**
          * 
          * 
          */
		pageScale,

		/**
         * 
         * 
         */
		pageCopyleft,

		/**
         * 
         * 
         */
		pageLegend;

		private String upper;

		MapLinkArg() {
			this.upper = toString().toUpperCase();
		}

		public String getUpper() {
			return this.upper;
		}
	}

	enum SettingsPart {

		Debug;
	}

	enum SettingsSetting {
		DumpJson;

		protected void apply(String value) {

		}

	}

	private static Log log = LogFactory.getLog(MapResource.class);

	@HeaderParam("X-Forwarded-For")
	private String xForwardedFor;

	@HeaderParam("Forwarded-For")
	private String forwardedFor;

	/* synchronized for create on call only */
	static Object getmapResourceLock = new Object();

	static WebServiceMapProducerResource shared;

	public static WebServiceMapProducerResource acquire()
			throws NoSuchAuthorityCodeException, IOException,
			GeoWebCacheException, FactoryException,
			com.vividsolutions.jts.io.ParseException {
		synchronized (getmapResourceLock) {
			if (shared == null) {

				String conf = System
						.getProperty(ConfigValue.CONFIG_SYSTEM_PROPERTY);

				Properties props = new Properties();
				Reader r = conf != null ? new FileReader(conf)
						: new InputStreamReader(
								MapResource.class
										.getResourceAsStream(ConfigValue.DEFAULT_PROPERTIES));
				try {
					props.load(r);
				} finally {
					r.close();
				}

				shared = new WebServiceMapProducerResource(props);

				URL layerJSONurl = new URL(
						ConfigValue.LAYERSURL.getConfigProperty(props));
				//
				shared.setLayerJSONurl(layerJSONurl);

				try {
					shared.loadLayerJson();
				} catch (com.vividsolutions.jts.io.ParseException geomEx) {
					if (shared.getLayerJson() != null) {
						/* we'll use the old one */
					} else {
						throw geomEx;
					}
				} catch (IOException ioe) {
					if (shared.getLayerJson() != null) {
						/* we'll use the old one */
					} else {
						throw ioe;
					}
				}

			}
		}

		return shared;
	}

	public MapResource() {

	}

	/**
	 * Input: URL parameters as paikkatietoikkuna de-facto maplink with extra
	 * parameters for printing: pageSize=A4|A4_Landscape|A3|A3_Landscape
	 * 
	 * Output: This outputs GeoJSON which describes printout extent. Extent is
	 * calculized based on parameters.
	 * 
	 * @param ui
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 * @throws GeoWebCacheException
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 * @throws RequestFilterException
	 * @throws TransformException
	 * @throws NoSuchAuthorityCodeException
	 * @throws FactoryException
	 * @throws com.vividsolutions.jts.io.ParseException
	 */
	@GET
	@Path("service/thumbnail/extent.jsonp")
	@Produces("application/json")
	public StreamingJSONImpl getSnapshotExtentJson(@Context UriInfo ui)
			throws IOException, ParseException, GeoWebCacheException,
			XMLStreamException, FactoryConfigurationError,
			RequestFilterException, TransformException,
			NoSuchAuthorityCodeException, FactoryException,
			com.vividsolutions.jts.io.ParseException {
		log.info("(X-)Forwarded-For " + forwardedFor + " / " + xForwardedFor);

		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		Map<String, String> values = new HashMap<String, String>();

		String[] mapLinkArgs = new String[] { "zoomLevel", "coord",
				"mapLayers", "width", "height", "scaledWidth", "scaledHeight",
				"bbox", "pageSize", "pageTitle", "pageLogo", "pageDate",
				"pageScale", "pageLegend", "pageCopyleft" };

		for (String mapLinkArg : mapLinkArgs) {
			String upper = new String(mapLinkArg).toUpperCase();
			if (queryParams.get(mapLinkArg) == null) {
				continue;
			}
			values.put(upper, queryParams.get(mapLinkArg).get(0));
		}
		/*
		 * for( MapLinkArg arg: MapLinkArg.values() ) { if (queryParams.get(arg)
		 * == null) { continue; } values.put(arg.getUpper(),
		 * queryParams.get(arg).get(0)); }
		 */
		WebServiceMapProducerResource getmap = acquire();

		return getmap.getMapExtentJSON(values, getXClientInfo());

	}

	/**
	 * Input: URL parameters as paikkatietoikkuna de-facto maplink with extra
	 * parameters for printing: pageSize=A4|A4_Landscape|A3|A3_Landscape
	 * 
	 * Output: This outputs PDF document with map layer images embedded as PDF
	 * optional content layers.
	 * 
	 * @param ui
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 * @throws GeoWebCacheException
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 * @throws RequestFilterException
	 * @throws TransformException
	 * @throws InterruptedException
	 * @throws NoSuchAuthorityCodeException
	 * @throws FactoryException
	 * @throws com.vividsolutions.jts.io.ParseException
	 * @throws URISyntaxException
	 */
	@GET
	@Path("service/thumbnail/maplink.pdf")
	@Produces("application/pdf")
	public StreamingOutput getSnapshotPDF(@Context UriInfo ui)
			throws IOException, ParseException, GeoWebCacheException,
			XMLStreamException, FactoryConfigurationError,
			RequestFilterException, TransformException, InterruptedException,
			NoSuchAuthorityCodeException, FactoryException,
			com.vividsolutions.jts.io.ParseException, URISyntaxException {

		log.info("(X-)Forwarded-For " + forwardedFor + " / " + xForwardedFor);

		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		Map<String, String> values = new HashMap<String, String>();

		String[] mapLinkArgs = new String[] { "zoomLevel", "coord",
				"mapLayers", "width", "height", "scaledWidth", "scaledHeight",
				"bbox", "pageSize", "pageTitle", "pageLogo", "pageDate",
				"pageScale", "pageLegend", "pageCopyleft" };

		for (String mapLinkArg : mapLinkArgs) {
			String upper = new String(mapLinkArg).toUpperCase();
			if (queryParams.get(mapLinkArg) == null) {
				continue;
			}
			values.put(upper, queryParams.get(mapLinkArg).get(0));
		}

		StreamingOutput result = null;
		WebServiceMapProducerResource getmap = acquire();

		result = getmap.getMapPDF(values, getXClientInfo());

		return result;

	}

	/**
	 * 
	 * Input: Input is a JSON document describing contents of the map document
	 * to be generated
	 * 
	 * Output: This outputs PDF document wiht embedded map images as PDF
	 * optional content layers. Input
	 * 
	 * @param json
	 * @return
	 * @throws NoSuchAuthorityCodeException
	 * @throws IOException
	 * @throws GeoWebCacheException
	 * @throws FactoryException
	 * @throws ParseException
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 * @throws RequestFilterException
	 * @throws TransformException
	 * @throws InterruptedException
	 * @throws com.vividsolutions.jts.io.ParseException
	 * @throws org.json.simple.parser.ParseException
	 * @throws URISyntaxException
	 */
	@POST
	@Path("service/thumbnail/maplinkgeojson.pdf")
	@Consumes("application/json")
	@Produces("application/pdf")
	public StreamingOutput getSnapshotPDFByActionRouteGeoJson(String json)
			throws NoSuchAuthorityCodeException, IOException,
			GeoWebCacheException, FactoryException, ParseException,
			XMLStreamException, FactoryConfigurationError,
			RequestFilterException, TransformException, InterruptedException,
			com.vividsolutions.jts.io.ParseException,
			org.json.simple.parser.ParseException, URISyntaxException {
		log.info("(X-)Forwarded-For " + forwardedFor + " / " + xForwardedFor);

		StreamingOutput result = null;
		WebServiceMapProducerResource getmap = acquire();

		/*
		 * 1) geojson processing
		 */

		/* 2) default processing (geojson enhanced ) */
		InputStream inp = new ByteArrayInputStream(json.getBytes());
		try {
			result = getmap.getGeoJsonMapPDF(inp, getXClientInfo());

		} finally {
			inp.close();
		}

		return result;

	}

	/**
	 * 
	 * Input: Input is a JSON document describing contents of the map document
	 * to be generated
	 * 
	 * Output: This outputs PDF document wiht embedded map images as PDF
	 * optional content layers. Input
	 * 
	 * @param json
	 * @return
	 * @throws NoSuchAuthorityCodeException
	 * @throws IOException
	 * @throws GeoWebCacheException
	 * @throws FactoryException
	 * @throws ParseException
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 * @throws RequestFilterException
	 * @throws TransformException
	 * @throws InterruptedException
	 * @throws com.vividsolutions.jts.io.ParseException
	 * @throws URISyntaxException
	 */
	@POST
	@Path("service/thumbnail/maplinkjson.pdf")
	@Consumes("application/json")
	@Produces("application/pdf")
	public StreamingOutput getSnapshotPDFByActionRouteJson(String json)
			throws NoSuchAuthorityCodeException, IOException,
			GeoWebCacheException, FactoryException, ParseException,
			XMLStreamException, FactoryConfigurationError,
			RequestFilterException, TransformException, InterruptedException,
			com.vividsolutions.jts.io.ParseException, URISyntaxException {
		log.info("(X-)Forwarded-For " + forwardedFor + " / " + xForwardedFor);

		StreamingOutput result = null;
		WebServiceMapProducerResource getmap = acquire();

		InputStream inp = new ByteArrayInputStream(json.getBytes());
		try {
			result = getmap.getMapPDF(inp, getXClientInfo());

		} finally {
			inp.close();
		}

		return result;

	}

	/**
	 * 
	 * Input: Input is a JSON document describing contents of the map document
	 * to be generated
	 * 
	 * Output: This outputs PNG image with anchored down map image layerss
	 * combined with opacities to resulting PNG image.
	 * 
	 * @param ui
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 * @throws GeoWebCacheException
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 * @throws RequestFilterException
	 * @throws TransformException
	 * @throws NoSuchAuthorityCodeException
	 * @throws FactoryException
	 * @throws com.vividsolutions.jts.io.ParseException
	 * @throws URISyntaxException
	 */
	@GET
	@Path("service/thumbnail/maplink.png")
	@Produces("image/png")
	public StreamingOutput getSnapshotPNG(@Context UriInfo ui)
			throws IOException, ParseException, GeoWebCacheException,
			XMLStreamException, FactoryConfigurationError,
			RequestFilterException, TransformException,
			NoSuchAuthorityCodeException, FactoryException,
			com.vividsolutions.jts.io.ParseException, URISyntaxException {
		log.info("(X-)Forwarded-For " + forwardedFor + " / " + xForwardedFor);

		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		Map<String, String> values = new HashMap<String, String>();

		String[] mapLinkArgs = new String[] { "zoomLevel", "coord",
				"mapLayers", "width", "height", "scaledWidth", "scaledHeight",
				"bbox", "pageSize", "pageTitle", "pageLogo", "pageDate",
				"pageScale", "pageLegend", "pageCopyleft" };

		for (String mapLinkArg : mapLinkArgs) {
			String upper = new String(mapLinkArg).toUpperCase();
			if (queryParams.get(mapLinkArg) == null) {
				continue;
			}
			values.put(upper, queryParams.get(mapLinkArg).get(0));
		}
		/*
		 * for( MapLinkArg arg: MapLinkArg.values() ) { if (queryParams.get(arg)
		 * == null) { continue; } values.put(arg.getUpper(),
		 * queryParams.get(arg).get(0)); }
		 */
		StreamingOutput result = null;
		WebServiceMapProducerResource getmap = acquire();

		result = getmap.getMapPNG(values, getXClientInfo());

		return result;

	}

	/**
	 * 
	 * Input: Input is a JSON document describing contents of the map document
	 * to be generated This endpoint rasterises embedded GeoJSON a
	 * 
	 * Output: This outputs PNG image with anchored down map image layerss
	 * combined with opacities to resulting PNG image.
	 * 
	 * @param json
	 * @return
	 * @throws IOException
	 * @throws NoSuchAuthorityCodeException
	 * @throws ParseException
	 * @throws GeoWebCacheException
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 * @throws RequestFilterException
	 * @throws TransformException
	 * @throws InterruptedException
	 * @throws FactoryException
	 * @throws com.vividsolutions.jts.io.ParseException
	 * @throws org.json.simple.parser.ParseException
	 * @throws URISyntaxException
	 */
	@POST
	@Path("service/thumbnail/maplinkgeojson.png")
	@Consumes("application/json")
	@Produces("image/png")
	public StreamingOutput getSnapshotPNGByActionRouteGeoJson(String json)
			throws IOException, NoSuchAuthorityCodeException, ParseException,
			GeoWebCacheException, XMLStreamException,
			FactoryConfigurationError, RequestFilterException,
			TransformException, InterruptedException, FactoryException,
			com.vividsolutions.jts.io.ParseException,
			org.json.simple.parser.ParseException, URISyntaxException {

		log.info("(X-)Forwarded-For " + forwardedFor + " / " + xForwardedFor);

		/*
		 * 1) geojson processing
		 */

		/* 2) default processing (geojson enhanced ) */

		StreamingOutput result = null;
		WebServiceMapProducerResource getmap = acquire();

		InputStream inp = new ByteArrayInputStream(json.getBytes());
		try {
			result = getmap.getGeoJsonMapPNG(inp, getXClientInfo());

		} finally {
			inp.close();
		}

		return result;
	}

	/**
	 * 
	 * Input: Input is a JSON document describing contents of the map document
	 * to be generated
	 * 
	 * Output: This outputs PNG image with anchored down map image layerss
	 * combined with opacities to resulting PNG image.
	 * 
	 * @param json
	 * @return
	 * @throws IOException
	 * @throws NoSuchAuthorityCodeException
	 * @throws ParseException
	 * @throws GeoWebCacheException
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 * @throws RequestFilterException
	 * @throws TransformException
	 * @throws InterruptedException
	 * @throws FactoryException
	 * @throws com.vividsolutions.jts.io.ParseException
	 * @throws URISyntaxException
	 */
	@POST
	@Path("service/thumbnail/maplinkjson.png")
	@Consumes("application/json")
	@Produces("image/png")
	public StreamingOutput getSnapshotPNGByActionRouteJson(String json)
			throws IOException, NoSuchAuthorityCodeException, ParseException,
			GeoWebCacheException, XMLStreamException,
			FactoryConfigurationError, RequestFilterException,
			TransformException, InterruptedException, FactoryException,
			com.vividsolutions.jts.io.ParseException, URISyntaxException {

		log.info("(X-)Forwarded-For " + forwardedFor + " / " + xForwardedFor);

		StreamingOutput result = null;
		WebServiceMapProducerResource getmap = acquire();

		InputStream inp = new ByteArrayInputStream(json.getBytes());
		try {
			result = getmap.getMapPNG(inp, getXClientInfo());

		} finally {
			inp.close();
		}

		return result;
	};

	/**
	 * 
	 * Input: Input is a JSON document describing contents of the map document
	 * to be generated * Output: This outputs PNG image with anchored down map
	 * image layerss combined with opacities to resulting PNG image embedded
	 * within PPTX slideshow. W-i-P.
	 * 
	 * @param ui
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 * @throws GeoWebCacheException
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 * @throws RequestFilterException
	 * @throws TransformException
	 * @throws InterruptedException
	 * @throws NoSuchAuthorityCodeException
	 * @throws FactoryException
	 * @throws com.vividsolutions.jts.io.ParseException
	 * @throws URISyntaxException
	 */
	@GET
	@Path("service/thumbnail/maplink.pptx")
	@Produces("application/vnd.openxmlformats-officedocument.presentationml.presentation")
	public StreamingOutput getSnapshotPPTX(@Context UriInfo ui)
			throws IOException, ParseException, GeoWebCacheException,
			XMLStreamException, FactoryConfigurationError,
			RequestFilterException, TransformException, InterruptedException,
			NoSuchAuthorityCodeException, FactoryException,
			com.vividsolutions.jts.io.ParseException, URISyntaxException {

		log.info("(X-)Forwarded-For " + forwardedFor + " / " + xForwardedFor);

		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		Map<String, String> values = new HashMap<String, String>();

		String[] mapLinkArgs = new String[] { "zoomLevel", "coord",
				"mapLayers", "width", "height", "scaledWidth", "scaledHeight",
				"bbox", "pageSize", "pageTitle", "pageLogo", "pageDate",
				"pageScale", "pageLegend", "pageCopyleft" };

		for (String mapLinkArg : mapLinkArgs) {
			String upper = new String(mapLinkArg).toUpperCase();
			if (queryParams.get(mapLinkArg) == null) {
				continue;
			}
			values.put(upper, queryParams.get(mapLinkArg).get(0));
		}

		StreamingOutput result = null;
		WebServiceMapProducerResource getmap = acquire();

		result = getmap.getMapPPTX(values, getXClientInfo());

		return result;

	}

	private String getXClientInfo() {
		if (forwardedFor != null) {
			return forwardedFor;
		}
		if (xForwardedFor != null) {
			return xForwardedFor;
		}
		return null;
	}

	/**
	 * endpoint to support reloading global layers list used when processing GET
	 * requests
	 * 
	 * @return
	 * @throws NoSuchAuthorityCodeException
	 * @throws IOException
	 * @throws GeoWebCacheException
	 * @throws FactoryException
	 * @throws com.vividsolutions.jts.io.ParseException
	 */
	@GET
	@Path("service/layers/reload")
	@Produces("application/json")
	public String reloadLayers() throws NoSuchAuthorityCodeException,
			IOException, GeoWebCacheException, FactoryException,
			com.vividsolutions.jts.io.ParseException {

		if (shared != null) {
			shared.setLayersDirty(true);
		}

		MapResource.acquire();

		if (shared != null && shared.getLayerJson() != null) {
			return "{ \"result\" : true, \"url\" : \""
					+ shared.getLayerJSONurl().toString() + "\" }";
		} else {
			return "{ \"result\" : false }";
		}
	}

	/**
	 * endpoint to support setting resetting some globals
	 * 
	 * @param part
	 * @param setting
	 * @param value
	 * @return
	 */
	@GET
	@Path("service/settings/{Part}/{Setting}/json")
	@Produces("application/json")
	public String settings(@PathParam("Part") String part,
			@PathParam("Setting") String setting,
			@QueryParam("value") String value) {

		boolean result = false;

		SettingsSetting settingsSetting = SettingsSetting.valueOf(setting);

		if (value != null) {
			/* write */
		} else {
			/* read */
			settingsSetting.apply(value);

		}

		return "{ \"result\" : " + (result ? "true" : "false") + " }";
	}

}
