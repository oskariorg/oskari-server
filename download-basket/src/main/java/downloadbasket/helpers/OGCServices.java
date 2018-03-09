package downloadbasket.helpers;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
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
	private static final String KEY_CROPPING_MODE = "croppingMode";
	private static final String KEY_CROPPING_LAYER = "croppingLayer";
	private static final String KEY_BBOX = "bbox";
	private static final String KEY_BBOX_LEFT = "left";
	private static final String KEY_BBOX_BOTTOM = "bottom";
	private static final String KEY_BBOX_RIGHT = "right";
	private static final String KEY_BBOX_TOP = "top";
	private static final String KEY_IDENTIFIERS = "identifiers";
	private static final String KEY_LAYER = "layer";
	private static final String KEY_GEOMETRY_COLUMN = "geometryColumn";
	private static final String KEY_GEOMETRY_COLUMN_NAME = "geometry";
	private static final String KEY_LAYER_NAME = "layerName";
	private static final String KEY_UNIQUE_COLUMN= "uniqueColumn";
	private static final String KEY_UNIQUE_VALUE = "uniqueValue";
	private static final String KEY_CROP_GEOMETRY_NAME = "geometryName";

	/**
	 * Get filter
	 * 
	 * @param downloadDetails
	 *            download details
	 * @param writeParam
	 *            write param
	 * @param oskariLayer oskari layer
	 *
	 * @return filter url param and value
	 * @throws JSONException
	 * @throws UnsupportedEncodingException
	 */
	public static String getFilter(JSONObject downloadDetails, Boolean writeParam, OskariLayer oskariLayer)
			throws JSONException, UnsupportedEncodingException {
		StringBuilder s = new StringBuilder();

		String[] bboxDownloadTypes = PropertyUtil.getCommaSeparatedList("oskari.wfs.download.normal.way.downloads");
		NormalWayDownloads normalDownloads = new NormalWayDownloads();
		for (String download : bboxDownloadTypes) {
			normalDownloads.addDownload(download);
		}

		final String croppingMode = downloadDetails.getString(KEY_CROPPING_MODE);
		String croppingLayer = "";
		if (downloadDetails.has(KEY_CROPPING_LAYER)) {
			croppingLayer = downloadDetails.getString(KEY_CROPPING_LAYER);
		}

		if (normalDownloads.isBboxCropping(croppingMode, croppingLayer)) {
			if (writeParam) {
				s.append("&bbox=");
			}
			s.append(getBbox(downloadDetails.getJSONObject(KEY_BBOX)));
		} else {
			if (writeParam) {
				s.append("&filter=");
			}
			s.append(URLEncoder.encode(getPluginFilter(downloadDetails, oskariLayer), "UTF-8"));
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
		return bbox.getString(KEY_BBOX_LEFT) + "," + bbox.getString(KEY_BBOX_BOTTOM) + ","
				+ bbox.getString(KEY_BBOX_RIGHT) + "," + bbox.getString(KEY_BBOX_TOP);
	}

	/**
	 * Get WFS Query layer WFS request by using GeoServer Cross-layer filtering
	 * plugin.
	 * 
	 * @param download
	 *            download details
	 * @param oskariLayer oskari layer
	 *            
	 */

	public static String getPluginFilter(JSONObject download, OskariLayer oskariLayer) throws JSONException {
		JSONArray identifiers = new JSONArray(download.getString(KEY_IDENTIFIERS));
		String xml = "";
		String croppingNameSpace = PropertyUtil.get("oskari.wfs.cropping.namespace");

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			String geometryColumnName = oskariLayer.getAttributes().optString(KEY_GEOMETRY_COLUMN_NAME, "SHAPE");

			XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(baos);
			String OGC = "http://www.opengis.net/ogc";
			xsw.setPrefix("ogc", OGC);

			if (identifiers.length() > 1) {
				xsw.writeStartElement(OGC, "Or");
			}
			for (int id = 0; id < identifiers.length(); id++) {
				JSONObject identifier = identifiers.getJSONObject(id);
				String layerName = Helpers.getLayerNameWithoutNameSpace(identifier.getString(KEY_LAYER_NAME));
				String uniqueColumn = identifier.getString(KEY_UNIQUE_COLUMN);
				String uniqueValue = identifier.getString(KEY_UNIQUE_VALUE);
				String cropGeomColumn = identifier.getString(KEY_CROP_GEOMETRY_NAME);
				String filterColumnType = identifier.getString(KEY_GEOMETRY_COLUMN);
				xsw.writeStartElement("Intersects");
				xsw.writeStartElement("PropertyName");
				xsw.writeCharacters(geometryColumnName);
				xsw.writeEndElement();
				xsw.writeStartElement("Function");
				xsw.writeAttribute("name", "querySingle");
				xsw.writeStartElement("Literal");
				xsw.writeCharacters(croppingNameSpace + ":" + layerName);
				xsw.writeEndElement();
				xsw.writeStartElement("Literal");
				xsw.writeCharacters(cropGeomColumn);
				xsw.writeEndElement();
				if (filterColumnType.equals("STRING")) {
					xsw.writeStartElement("Literal");
					xsw.writeCharacters(uniqueColumn + " LIKE '" + uniqueValue.trim() + "%" + "'");
					xsw.writeEndElement();
				} else {
					xsw.writeStartElement("Literal");
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
	 * @param srs
	 *            srs name
	 * @param download
	 *            download details
	 * @param addNameSpace
	 *            add namespace to layer name
	 * @return WFS GET feature URL
	 */
	public static String doGetFeatureUrl(String srs, JSONObject download, boolean addNameSpace) throws JSONException {
		String getFeatureUrl = "";
		StringBuilder s = new StringBuilder();
		s.append(PropertyUtil.get("oskari.wfs.service.url"));

		s.append("?SERVICE=wfs&version=1.0.0&request=GetFeature&srsName=");
		s.append(srs);
		s.append("&outputFormat=SHAPE-ZIP&typeNames=");

		if (addNameSpace) {
			s.append(download.getString(KEY_LAYER));
		} else {
			s.append(Helpers.getLayerNameWithoutNameSpace(download.getString(KEY_LAYER)));
		}
		getFeatureUrl = s.toString();
		return getFeatureUrl;
	}
}