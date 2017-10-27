package downloadbasket.helpers;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.util.PropertyUtil;
import downloadbasket.data.NormalWayDownloads;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.nio.charset.StandardCharsets;
import java.lang.StringBuilder;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class OGCServices {
	private static final fi.nls.oskari.log.Logger LOGGER = LogFactory.getLogger(OGCServices.class);

	private static final String PARAM_CROPPING_MODE = "croppingMode";
	private static final String PARAM_CROPPING_LAYER = "croppingLayer";
	private static final String PARAM_BBOX = "bbox";
	private static final String PARAM_BBOX_LEFT = "left";
	private static final String PARAM_BBOX_BOTTOM = "bottom";
	private static final String PARAM_BBOX_RIGHT = "right";
	private static final String PARAM_BBOX_TOP = "top";
	private static final String PARAM_IDENTIFIERS = "identifiers";
	private static final String PARAM_LAYER = "layer";

	/**
	 * Get filter
	 * 
	 * @param downloadDetails
	 *            download details
	 * @param writeParam
	 *            write param
	 *
	 * @return filter url param and value
	 */
	public static String getFilter(JSONObject downloadDetails, Boolean writeParam)
			throws JSONException, UnsupportedEncodingException {
		StringBuilder s = new StringBuilder();

		String[] bboxDownloadTypes = PropertyUtil.getCommaSeparatedList("oskari.wfs.bbox.downloads");
		NormalWayDownloads normalDownloads = new NormalWayDownloads();
		for (String download : bboxDownloadTypes) {
			normalDownloads.addDownload(download);
		}

		final String croppingMode = downloadDetails.getString(PARAM_CROPPING_MODE);
		String croppingLayer = "";
		if (downloadDetails.has(PARAM_CROPPING_LAYER)) {
			croppingLayer = downloadDetails.getString(PARAM_CROPPING_LAYER);
		}

		if (normalDownloads.isBboxCropping(croppingMode, croppingLayer)) {
			if (writeParam) {
				s.append("&bbox=");
			}
			s.append(getBbox(downloadDetails.getJSONObject(PARAM_BBOX)));
		} else {
			if (writeParam) {
				s.append("&filter=");
			}
			s.append(URLEncoder.encode(getPluginFilter(downloadDetails), "UTF-8"));
		}

		return s.toString();
	}

	/**
	 * Get bbox
	 * 
	 * @param bbox
	 * @return
	 * @throws JSONException
	 */
	private static String getBbox(JSONObject bbox) throws JSONException {
		return bbox.getString(PARAM_BBOX_LEFT) + "," + bbox.getString(PARAM_BBOX_BOTTOM) + ","
				+ bbox.getString(PARAM_BBOX_RIGHT) + "," + bbox.getString(PARAM_BBOX_TOP);
	}

	/**
	 * Get WFS Query layer WFS request by using GeoServer Cross-layer filtering plugin.
	 * 
	 * @param download
	 *            download details
	 */
	
	public static String getPluginFilter(JSONObject download)
			throws JSONException {
		JSONArray identifiers = new JSONArray(download.getString(PARAM_IDENTIFIERS));
		String xml = "";
		String croppingNameSpace = PropertyUtil.get("oskari.wfs.cropping.namespace");

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(baos);
			String OGC = "http://www.opengis.net/ogc";
			xsw.setPrefix("ogc", OGC);
			
			if (identifiers.length() > 1) {
				xsw.writeStartElement(OGC, "Or");
			}

			for (int id = 0; id < identifiers.length(); id++) {
				JSONObject identifier = identifiers.getJSONObject(id);
				String layerName = Helpers.getLayerNameWithoutNameSpace(identifier.getString("layerName"));
				String uniqueColumn = identifier.getString("uniqueColumn");
				String uniqueValue = identifier.getString("uniqueValue");
				String cropGeomColumn = identifier.getString("geometryName");
				String filterColumnType = identifier.getString("geometryColumn");
				xsw.writeStartElement(OGC, "Intersects");
				
				xsw.writeStartElement(OGC, "PropertyName");
				xsw.writeCharacters(cropGeomColumn);
				xsw.writeEndElement();
				xsw.writeStartElement(OGC, "Function");
				xsw.writeAttribute("name", "querySingle");				
				xsw.writeStartElement(OGC, "Literal");
				xsw.writeCharacters(croppingNameSpace + ":" + layerName);
				xsw.writeEndElement();
				xsw.writeStartElement(OGC, "Literal");
				xsw.writeCharacters(cropGeomColumn);
				xsw.writeEndElement();
				if (filterColumnType.equals("STRING")) {
					xsw.writeStartElement(OGC, "Literal");
					xsw.writeCharacters(uniqueColumn + " LIKE '" + uniqueValue.trim() + "%" + "'");
					xsw.writeEndElement();
				} else {
					xsw.writeStartElement(OGC, "Literal");
					xsw.writeCharacters(uniqueColumn + " =  " + uniqueValue);
					xsw.writeEndElement();
				}
				xsw.writeEndElement();
				xsw.writeEndElement();
			}

			if (identifiers.length() > 1) {
				xsw.writeEndElement();
			}

			xsw.close();

			xml = baos.toString(StandardCharsets.UTF_8.name());
			LOGGER.debug("Created plugin filter:" + xml);

		} catch (Exception e) {
			LOGGER.error(e, "Error");
		}
		return xml;
	}

	/**
	 * GetFeature URL
	 * 
	 * @param wfsUrl
	 *            WFS url
	 * @param download
	 *            download details
	 * @param addNameSpace
	 *            add namespace to layer name
	 * @return WFS GET feature URL
	 */
	public static String doGetFeatureUrl(String srs, JSONObject download, boolean addNameSpace) throws JSONException {
		String getFeatureUrl = "";
		StringBuilder s = new StringBuilder();
		s.append(PropertyUtil.get("download.basket.wfs.service.url"));

		s.append("?SERVICE=wfs&version=1.0.0&request=GetFeature&srsName=");
		s.append(srs);
		s.append("&outputFormat=SHAPE-ZIP&typeNames=");

		if (addNameSpace) {
			s.append(download.getString(PARAM_LAYER));
		} else {
			s.append(Helpers.getLayerNameWithoutNameSpace(download.getString(PARAM_LAYER)));
		}
		getFeatureUrl = s.toString();
		return getFeatureUrl;
	}
}
