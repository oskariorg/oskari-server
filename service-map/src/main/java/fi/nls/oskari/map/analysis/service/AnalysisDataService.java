package fi.nls.oskari.map.analysis.service;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.Layer;
import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.domain.map.analysis.AnalysisStyle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.analysis.domain.AnalysisLayer;
import fi.nls.oskari.map.analysis.domain.AnalysisMethodParams;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AnalysisDataService {
    private static final String ANALYSIS_INPUT_TYPE_GS_VECTOR = "gs_vector";
    private static final String ANALYSIS_LAYERTYPE = "analysislayer";
    private static final String JSKEY_ANALYSISLAYERS = "analysislayers";
    private static final String JSKEY_WPSLAYERID = "wpsLayerId";
    private static final String JSKEY_LAYERID = "layerId";

    private static final String JSKEY_NAME = "name";
    private static final String JSKEY_TYPE = "type";
    private static final String JSKEY_OPACITY = "opacity";
    private static final String JSKEY_MINSCALE = "minScale";
    private static final String JSKEY_MAXSCALE = "maxScale";
    private static final String JSKEY_FIELDS = "fields";

    private static final String JSKEY_ID = "id";
    private static final String JSKEY_SUBTITLE = "subtitle";
    private static final String JSKEY_ORGNAME = "orgname";
    private static final String JSKEY_INSPIRE = "inspire";
    private static final String JSKEY_WPSURL = "wpsUrl";
    private static final String JSKEY_WPSNAME = "wpsName";
    private static final String JSKEY_RESULT = "result";

    private static final String LAYER_PREFIX = "analysis_";
    private static final String ANALYSIS_BASELAYER_ID = "analysis.baselayer.id";
    private static final String ANALYSIS_RENDERING_URL = "analysis.rendering.url";
    private static final String ANALYSIS_ORGNAME = "";  // managed in front
    private static final String ANALYSIS_INSPIRE = "";  // managed in front
    private static final String ANALYSIS_WPS_ELEMENT_NAME = "ana:analysis_data";

    private static final Logger log = LogFactory
            .getLogger(AnalysisDataService.class);
    private static final String WFSTTEMPLATESTART = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<wfs:Transaction xmlns:wfs=\"http://www.opengis.net/wfs\" service=\"WFS\" version=\"1.1.0\" xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:cgf=\"http://www.opengis.net/cite/geometry\" >\n";

    private static final String WFSTTEMPLATEEND = "</wfs:Transaction>\n";

    private static final String WFSTINSERTSTART = "   <wfs:Insert>\n"
            + "       <feature:analysis_data xmlns:feature=\"http://nls.paikkatietoikkuna.fi/analysis\">\n";

    private static final String WFSTINSERTEND = "       </feature:analysis_data>\n"
            + "   </wfs:Insert>\n";

    private static final AnalysisStyleDbService styleService = new AnalysisStyleDbServiceIbatisImpl();
    private static final AnalysisDbService analysisService = new AnalysisDbServiceIbatisImpl();

    final String analysisBaseLayerId = PropertyUtil.get(ANALYSIS_BASELAYER_ID);
    final String analysisRenderingUrl = PropertyUtil.get(ANALYSIS_RENDERING_URL);

    public Analysis storeAnalysisData(final String featureset,
            AnalysisLayer analysislayer, String json, User user) {

        final String wfsURL = PropertyUtil.get("geoserver.wfs.url");
        final String wpsUser = PropertyUtil.get("geoserver.wms.user");
        final String wpsUserPass = PropertyUtil.get("geoserver.wms.pass");

        final AnalysisStyle style = new AnalysisStyle();
        final Analysis analysis = new Analysis();

        try {
            // Insert style row
            final JSONObject stylejs = JSONHelper
                    .createJSONObject(analysislayer.getStyle());
            style.populateFromJSON(stylejs);
        } catch (JSONException e) {
            log.debug("Unable to get AnalysisLayer style JSON", e);
        }
        // FIXME: do we really want to insert possibly empty style??
        log.debug("Adding style", style);
        styleService.insertAnalysisStyleRow(style);

        try {
            // Insert analysis row
            // --------------------
            analysis.setAnalyse_json(json.toString());
            analysis.setLayer_id(analysislayer.getId());
            analysis.setName(analysislayer.getName());
            analysis.setStyle_id(style.getId());
            analysis.setUuid(user.getUuid());
            log.debug("Adding analysis row", analysis);
            analysisService.insertAnalysisRow(analysis);

            // Add analysis_data rows via WFS-T
            // ----------------------------------

            // Convert featureset (wps) to wfs-t
            // ----------------------------------
            final AnalysisMethodParams params = analysislayer
                    .getAnalysisMethodParams();
            final String geometryProperty = stripNamespace(params.getGeom());
            // FIXME: wpsToWfst populates fields list AND returns the wfst
            // payload
            // this should be refactored so it returns an object with the fields
            // list and the payload
            // and remove the fields parameter from call
            List<String> fields = new ArrayList<String>();
            final String wfst = wpsToWfst(featureset, analysis.getUuid(),
                    analysis.getId(), fields, geometryProperty);
            log.debug("Produced WFS-T:\n" + wfst);

            final String response = IOHelper.httpRequestAction(wfsURL, wfst,
                    wpsUser, wpsUserPass, null, null, "application/xml");
            log.debug("Posted WFS-T, got", response);

            // Update col mapping and WPS layer Id into analysis table
            // ---------------------------------------
            // if analysis in analysis - fix field names to original
            if (analysislayer.getInputType().equals(
                    ANALYSIS_INPUT_TYPE_GS_VECTOR))
                fields = this.SwapAnalysisInAnalysisFields(fields,
                        analysislayer.getInputAnalysisId());
            analysis.setCols(fields);

            log.debug("Update analysis row", analysis);
            int updrows = analysisService.updateAnalysisCols(analysis);
            log.debug("Updated rows", updrows);

        } catch (Exception e) {
            log
                    .debug(
                            "Unable to transform WPS to WFS-T or to store analysis data",
                            e);
        }

        return analysis;
    }

    private String wpsToWfst(String wps, String uuid, long analysis_id,
            List<String> fields, String geometryProperty)
            throws ServiceException {

        final Document wpsDoc = createDoc(wps);
        StringBuilder sb = new StringBuilder(WFSTTEMPLATESTART);
        List<String> cols = new ArrayList<String>();
        List<String> geomcols = new ArrayList<String>();
        geomcols.add("feature:" + geometryProperty);

        String geomcol = "";

        // iterate through wpsDoc's gml:featureMember elements
        // NodeList featureMembers =
        // wpsDoc.getDocumentElement().getChildNodes();
        NodeList featureMembers = wpsDoc
                .getElementsByTagName("gml:featureMember");
        for (int i = 0; i < featureMembers.getLength(); i++) {
            // we're only interested in featureMembers...
            if (!"gml:featureMember".equals(featureMembers.item(i)
                    .getNodeName())) {
                continue;
            }
            // get features, i.e. all child elements in feature namespace
            // we trust that featureMember only has one feature...
            // find the child feature...
            NodeList meh = featureMembers.item(i).getChildNodes();
            NodeList features = null;
            for (int j = 0; j < meh.getLength(); j++) {
                if (meh.item(j).getNodeName().indexOf("feature:") == 0) {
                    features = meh.item(j).getChildNodes();
                    break;
                }
            }
            Node geometry = null;
            List<String> textFeatures = new ArrayList<String>();
            List<Double> numericFeatures = new ArrayList<Double>();
            int ncount = 1;
            int tcount = 1;
            for (int j = 0; j < features.getLength(); j++) {
                Node feature = features.item(j);

                // it's a feature, check if it's geometry
                if (geomcols.contains(feature.getNodeName())) {
                    // geometry, store aside for now
                    geomcol = feature.getNodeName();
                    geometry = feature;
                } else if (feature.getNodeName().indexOf("feature:") == 0) {
                    // only parse 8 first text and/or numeric features
                    // (excluding geometry)
                    if (textFeatures.size() < 8 && numericFeatures.size() < 8) {
                        // get node value
                        String strVal = feature.getTextContent();
                        Double dblVal = null;
                        // see if it's numeric
                        try {
                            dblVal = Double.parseDouble(strVal);
                        } catch (NumberFormatException nfe) {
                            // ignore
                        }
                        if (null != dblVal) {
                            numericFeatures.add(dblVal);
                            String col = feature.getNodeName();
                            String[] acol = feature.getNodeName().split(":");
                            if (acol.length > 1)
                                col = acol[1];
                            if (!cols.contains(col)) {
                                String colmap = "n" + Integer.toString(ncount)
                                        + "=" + col;
                                cols.add(col);
                                fields.add(colmap);
                                ncount++;
                            }
                        } else {
                            textFeatures.add(strVal);
                            String col = feature.getNodeName();
                            String[] acol = feature.getNodeName().split(":");
                            if (acol.length > 1)
                                col = acol[1];
                            if (!cols.contains(col)) {
                                String colmap = "t" + Integer.toString(tcount)
                                        + "=" + col;
                                cols.add(col);
                                fields.add(colmap);
                                tcount++;
                            }
                        }
                    }
                }
            }
            // build wfs:Insert element
            sb.append(WFSTINSERTSTART);
            // add geometry node
            sb.append(nodeToString(geometry).replace(geomcol,
                    "feature:geometry"));
            // add text feature nodes (1-based)
            for (int j = 0; j < textFeatures.size(); j++) {
                sb
                        .append("         <feature:t" + (j + 1) + ">"
                                + textFeatures.get(j) + "</feature:t" + (j + 1)
                                + ">\n");
            }
            // add numeric feature nodes (1-based)
            for (int j = 0; j < numericFeatures.size(); j++) {
                sb.append("         <feature:n" + (j + 1) + ">"
                        + numericFeatures.get(j) + "</feature:n" + (j + 1)
                        + ">\n");
            }

            sb.append("         <feature:analysis_id>"
                    + Long.toString(analysis_id) + "</feature:analysis_id>\n");

            sb.append("         <feature:uuid>" + uuid + "</feature:uuid>\n");

            sb.append(WFSTINSERTEND);
        }
        sb.append(WFSTTEMPLATEEND);
        return sb.toString();
    }

    private static String nodeToString(Node node) throws ServiceException {
        try {
            StringWriter sw = new StringWriter();
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(node), new StreamResult(sw));
            return sw.toString();
        } catch (Exception ex) {
            throw new ServiceException("Unable to write XML node to string");
        }
    }

    private String stripNamespace(final String tag) {

        String splitted[] = tag.split(":");
        if (splitted.length > 1) {
            return splitted[1];
        }
        return splitted[0];
    }

    private Document createDoc(final String content) throws ServiceException {
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory
                    .newInstance();
            // dbf.setNamespaceAware(true);
            final DocumentBuilder builder = dbf.newDocumentBuilder();
            final Document wpsDoc = builder.parse(new InputSource(
                    new ByteArrayInputStream(content.getBytes("UTF-8"))));
            return wpsDoc;
        } catch (Exception ex) {
            throw new ServiceException("Unable to create XML doc from content");
        }
    }

    /**
     * Get analysis columns to Map
     * @param analysis_id  Key to one analysis 
     * @return analysis columns
     */
    public Map<String, String> getAnalysisColumns(final String analysis_id) {
        if (analysis_id != null) {
            final Map<String, String> columnNames = new ConcurrentHashMap<String, String>(); // key,
            // name
            Analysis analysis = analysisService
                    .getAnalysisById(ConversionHelper.getLong(analysis_id, 0));
            if (analysis != null) {
                for (int j = 1; j < 11; j++) {
                    String colx = analysis.getColx(j);
                    if (colx != null && !colx.isEmpty()) {
                        if (colx.indexOf("=") != -1) {
                            columnNames.put(colx.split("=")[0],
                                    colx.split("=")[1]);
                        }
                    }

                }
                return columnNames;
            }
        }
        return null;
    }

    /**
     * @param fieldsin
     *            raw field names mapping
     * @param analysis_id
     *            analysis_id of input analysis
     * @return List of field names mapping
     */
    public List<String> SwapAnalysisInAnalysisFields(List<String> fieldsin,
            String analysis_id) {

        Map<String, String> colnames = this.getAnalysisColumns(analysis_id);

        for (int i = 0; i < fieldsin.size(); i++) {
            String col = fieldsin.get(i);
            if (!col.isEmpty()) {
                if (col.indexOf("=") != -1) {
                    String[] cola = col.split("=");
                    if (colnames.containsKey(cola[1])) {
                        fieldsin.set(i, cola[0] + "=" + colnames.get(cola[1]));
                    }

                }
            }

        }
        return fieldsin;
    }

    /**
     * @param field_in
     *            original field name
     * @param analysis_id
     *            analysis_id of input analysis
     * @return List of field names mapping
     */
    public String SwitchField2AnalysisField(String field_in, String analysis_id) {

        Map<String, String> colnames = this.getAnalysisColumns(analysis_id);

        for (Map.Entry<String, String> entry : colnames.entrySet()) {
            String key = entry.getKey();
            if (entry.getValue().toUpperCase().equals(field_in.toUpperCase())) {
                return key;
            }

        }

        return field_in;
    }

    /**
     * @param uid User uuid
     * @param lang language
     * @return Analysis layers of one user retreaved by uuid
     * @throws ServiceException
     */
    public JSONObject getListOfAllAnalysisLayers(String uid, String lang)
            throws ServiceException {

        final JSONObject listLayer = new JSONObject();
        try {
            List<Analysis> layers = analysisService.getAnalysisByUid(uid);
            for (Analysis al : layers) {
                final JSONObject analyse_js = JSONHelper.createJSONObject(al
                        .getAnalyse_json());
                // Parse analyse layer json out of analyse json
                 
                JSONObject analyselayer = getlayerJSON(analyse_js, al.getId());
                listLayer.accumulate(JSKEY_ANALYSISLAYERS, analyselayer);
            }
        } catch (Exception ex) {
            throw new ServiceException("Unable to get analysis layers", ex);
        }
        return listLayer;
    }

    // Analyse json sample
    // {"name":"Analyysi_Tampereen ","method":"buffer","fields":["__fid","metaDataProperty","description","name","boundedBy","location","NIMI","GEOLOC","__centerX","__centerY"],"layerId":264,"layerType":"wfs","methodParams":{"distance":"22"},"opacity":100,"style":{"dot":{"size":"4","color":"CC9900"},"line":{"size":"2","color":"CC9900"},"area":{"size":"2","lineColor":"CC9900","fillColor":"FFDC00"}},"bbox":{"left":325158,"bottom":6819828,"right":326868,"top":6820378}}
    /**
     * @param analyse_js  analyse wps parameters
     * @param wpsid  analysis_id
     * @return analysis layer data for front mapservice
     * @throws JSONException
     */
    public JSONObject getlayerJSON(JSONObject analyse_js, Long wpsid)
    
            throws JSONException {
        JSONObject json = new JSONObject();
        // Add correct analyse layer_id to json
        try {
            String newid = "-1";
            if (analyse_js.has(JSKEY_LAYERID)) {
                if (analyse_js.getString(JSKEY_LAYERID).indexOf(LAYER_PREFIX) > -1)
                // analyse in Analysislayer (prefix + base analysis wfs layer id
                // +
                // analysis_id)
                {
                    newid = LAYER_PREFIX + analysisBaseLayerId + "_"
                            + String.valueOf(wpsid);
                } else {
                    // analyse in wfs layer (prefix + wfs layer id +
                    // analysis_id)
                    newid = LAYER_PREFIX + analyse_js.getString(JSKEY_LAYERID)
                            + "_" + String.valueOf(wpsid);
                }

            }

            json.put(JSKEY_ID, newid);
            json.put(JSKEY_TYPE, ANALYSIS_LAYERTYPE);

            json.put(JSKEY_NAME, JSONHelper.getStringFromJSON(analyse_js,
                    JSKEY_NAME, "n/a"));
            json.put(JSKEY_SUBTITLE, "");
            json.put(JSKEY_ORGNAME, ANALYSIS_ORGNAME);
            json.put(JSKEY_INSPIRE, ANALYSIS_INSPIRE);

            json.put(JSKEY_OPACITY, ConversionHelper.getInt(JSONHelper
                    .getStringFromJSON(analyse_js, JSKEY_OPACITY, "80"), 80));
            json.put(JSKEY_MINSCALE, ConversionHelper.getDouble(JSONHelper
                    .getStringFromJSON(analyse_js, JSKEY_MINSCALE, "1500000"),
                    1500000));
            json.put(JSKEY_MAXSCALE, ConversionHelper.getDouble(JSONHelper
                    .getStringFromJSON(analyse_js, JSKEY_MAXSCALE, "1"), 1));
            json.put(JSKEY_FIELDS, JSONHelper.getJSONArray(analyse_js,
                    JSKEY_FIELDS));

            json.put(JSKEY_WPSURL, analysisRenderingUrl);
            json.put(JSKEY_WPSNAME, ANALYSIS_WPS_ELEMENT_NAME);
            json.put(JSKEY_WPSLAYERID, wpsid);
            json.put(JSKEY_RESULT, "");
        } catch (Exception ex) {
            log.debug("Unable to get analysis layer json", ex);
        }

        return json;
    }

}
