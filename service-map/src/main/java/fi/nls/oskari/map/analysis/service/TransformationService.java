package fi.nls.oskari.map.analysis.service;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.XmlHelper;
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
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TransformationService {

    // "created" and "updated" added here to prevent them from appearing on the created analysis
    private static final List<String> HIDDEN_FIELDS = Arrays.asList("analysis_id", "created", "updated");
    private static final String NUMERIC_FIELD_TYPE = "numeric";

    private static final Logger log = LogFactory
            .getLogger(TransformationService.class);
    private static final String WFSTTEMPLATESTART = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<wfs:Transaction xmlns:wfs=\"http://www.opengis.net/wfs\" service=\"WFS\" version=\"1.1.0\" xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:cgf=\"http://www.opengis.net/cite/geometry\" >\n";

    private static final String WFSTTEMPLATEEND = "</wfs:Transaction>\n";

    private static final String WFSTINSERTSTART = "   <wfs:Insert>\n"
            + "       <feature:analysis_data xmlns:feature=\"http://www.oskari.org\">\n";

    private static final String WFSTINSERTEND = "       </feature:analysis_data>\n"
            + "   </wfs:Insert>\n";


    public String wpsFeatureCollectionToWfst(final  String wps , String uuid, long analysis_id,
            List<String> fields, Map<String,String> fieldTypes, String geometryProperty, String ns_prefix)
            throws ServiceException {

        final Document wpsDoc = createDoc(wps);
        StringBuilder sb = new StringBuilder(WFSTTEMPLATESTART);
        List<String> cols = new ArrayList<String>();
        List<String> geomcols = new ArrayList<String>();
        geomcols.add(ns_prefix+":" + geometryProperty);
        geomcols.add(ns_prefix+":geometry");  //  default geometry

        String geomcol = "";
        Boolean members_case = false;
        int ncount = 0;
        int tcount = 0;

        // iterate through wpsDoc's gml:featureMember elements or gml:featureMembers
        // NodeList featureMembers =
        // wpsDoc.getDocumentElement().getChildNodes();
        NodeList featureMembers = wpsDoc
                .getElementsByTagName("gml:featureMember");
        if (featureMembers.getLength() == 0) {
            featureMembers = wpsDoc
                    .getElementsByTagName("gml:featureMembers");
            if (featureMembers.getLength() > 0) members_case = true;
        }
        if(members_case){
            // select features as featureMembers
            featureMembers = wpsDoc.getElementsByTagName( featureMembers.item(0).getFirstChild().getNodeName());
        }
        for (int i = 0; i < featureMembers.getLength(); i++) {
            // we're only interested in featureMembers... or features
            if (!"gml:featureMember".equals(featureMembers.item(i)
                    .getNodeName()) &&  !members_case ) {
                continue;
            }

            NodeList meh = featureMembers.item(i).getChildNodes();
            NodeList features = null;
            if (members_case) {
                // gml:featureMembers case - all features under one element
                features = meh;
            } else {
                // get features, i.e. all child elements in feature namespace
                // we trust that featureMember only has one feature...
                // find the child feature...
                for (int j = 0; j < meh.getLength(); j++) {
                    if (meh.item(j).getNodeName().indexOf(ns_prefix + ":") == 0) {
                        features = meh.item(j).getChildNodes();
                        break;
                    }
                }
            }
            Node geometry = null;
            List<String> textFeatures = new ArrayList<String>();
            List<Double> numericFeatures = new ArrayList<Double>();
            if ( ncount == 0) {
                ncount = 1;
                tcount = 1;
            }
            for (int j = 0; j < features.getLength(); j++) {
                Node feature = features.item(j);

                // it's a feature, check if it's geometry
                if (geomcols.contains(feature.getNodeName())) {
                    // geometry, store aside for now
                    geomcol = feature.getNodeName();
                    geometry = feature;
                } else if (feature.getNodeName().indexOf(ns_prefix+":") == 0) {
                    // only parse 8 first text ( numeric results invalid behavior later use only text)
                    //TODO: fix management of Date dateTime types later
                    // (excluding geometry)
                    if (textFeatures.size() < 8 && numericFeatures.size() < 8 && this.isHiddenField(feature) == false) {
                        // get node value
                        String strVal = feature.getTextContent();
                        Double dblVal = null;
                        String col = this.stripNamespace(feature.getNodeName());
                        dblVal = this.getFieldAsNumeric(col, strVal, fieldTypes);

                        if (null != dblVal) {
                            numericFeatures.add(dblVal);
                            if (!cols.contains(col)) {
                                String colmap = "n" + Integer.toString(ncount)
                                        + "=" + col;
                                cols.add(col);
                                fields.add(colmap);
                                ncount++;
                            }
                        } else {
                            textFeatures.add(strVal);
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
            buildWfsInsertElement(sb, geometry, geomcol, textFeatures, numericFeatures, uuid, analysis_id);
        }
        sb.append(WFSTTEMPLATEEND);
        return sb.toString();
    }

    private static String nodeToString(Node node) throws ServiceException {
        try {
            StringWriter sw = new StringWriter();
            Transformer t = XmlHelper.newTransformerFactory().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(node), new StreamResult(sw));
            return sw.toString();
        } catch (Exception ex) {
            throw new ServiceException("Unable to write XML node to string");
        }
    }
    private Document createDoc(final String content) throws ServiceException {
        try {
            final DocumentBuilderFactory dbf = XmlHelper.newDocumentBuilderFactory();
            // dbf.setNamespaceAware(true);
            final DocumentBuilder builder = dbf.newDocumentBuilder();
            final Document wpsDoc = builder.parse(new InputSource(
                    new ByteArrayInputStream(content.getBytes("UTF-8"))));
            return wpsDoc;
        } catch (Exception ex) {
            throw new ServiceException("Unable to create XML doc from content");
        }
    }
    public String stripNamespace(final String tag) {

        String splitted[] = tag.split(":");
        if (splitted.length > 1) {
            return splitted[1];
        }
        return splitted[0];
    }

    private boolean isHiddenField(Node feature)
    {
        String[] acol = feature.getNodeName().split(":");
        if (acol.length > 1) return HIDDEN_FIELDS.contains(acol[1]);

        return false;
    }

    /**
     *
     * @param fieldName
     * @param fieldTypes  field types like in WFS DescribeFeatureType
     * @return true, if numeric value (int,double,long,..)
     */
    private Double getFieldAsNumeric(String fieldName, String strVal, Map<String, String> fieldTypes) {
        Double numericValue = null;
        if (fieldTypes.containsKey(fieldName)) {
            //Check type
            if (fieldTypes.get(fieldName).equals(NUMERIC_FIELD_TYPE)) {
                try {
                    numericValue = Double.parseDouble(strVal);
                } catch (NumberFormatException nfe) {
                    // ignore
                }
            }
        } else {
            // Field name could be composed with layer name
            for (Map.Entry<String, String> entry : fieldTypes.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (fieldName.lastIndexOf(key) > 0 && value.equals(NUMERIC_FIELD_TYPE)) {
                    try {
                        numericValue = Double.parseDouble(strVal);
                    } catch (NumberFormatException nfe) {
                        // ignore
                    }
                }
            }
        }
        return numericValue;
    }

    /**
     *
     * @param featureSet      WPS union response
     * @param aggregateResults   Aggregate results ([attribue1:{Count:xx,Sum=yy,..},..])
     * @return
     * @throws ServiceException
     */
    public String addPropertiesTo1stFeature(String featureSet, String aggregateResults) throws ServiceException

    {

        final Document wpsDoc = createDoc(featureSet);
        try {

            NodeList featureMembers = wpsDoc.getElementsByTagName("gml:featureMember");
            for (int i = 0; i < featureMembers.getLength(); i++) {
                // we're only interested in featureMembers...
                if (!"gml:featureMember".equals(featureMembers.item(i)
                        .getNodeName())) {
                    continue;
                }
                // get features, i.e. all child elements in feature namespace
                // we trust that featureMember only has one feature...
                // find the child feature...

                JSONObject js = new JSONObject(aggregateResults);
                NodeList meh = featureMembers.item(i).getChildNodes();
                Node feature = null;
                for (int j = 0; j < meh.getLength(); j++) {
                    if (meh.item(j).getNodeName().indexOf("feature:") == 0) {
                        feature = meh.item(j);
                        break;
                    }
                }

                Iterator<?> keys = js.keys();

                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    if (js.get(key) instanceof JSONObject) {
                        JSONObject subjs = js.getJSONObject(key);

                            Node elem = feature.getOwnerDocument().createElement("feature:" + key);
                            elem.setTextContent(subjs.toString());
                            feature.appendChild(elem);

                    }
                }


            }
        } catch (JSONException e) {
            log.debug("Aggregate feature property insertion failed", e);
        }
        return this.getStringFromDocument(wpsDoc);
    }

    /**
     * Copy union feature geometry to each aggregate result and add aggregate results as feature properties
     *
     * @param featureSet       WPS union response
     * @param aggregateResults Aggregate results ([attribue1:{Count:xx,Sum=yy,..},..])
     * @return
     * @throws ServiceException
     */
    public String mergePropertiesToFeatures(String featureSet, String aggregateResults, List<String> rowOrder, List<String> colOrder) throws ServiceException

    {

        // Col order values
        Map<String, Node> colvalues = new HashMap<String, Node>();
        for (String col : colOrder) {
            colvalues.put(col, null);
        }


        final Document wpsDoc = createDoc(featureSet);
        try {
            JSONObject js = new JSONObject(aggregateResults);
            NodeList featureMembers = wpsDoc.getElementsByTagName("gml:featureMember");
            // We trust that union is in one featureMember
            // Clone 1st fea member so that there is one fea each aggregate result
            if (featureMembers.getLength() > 0) {
                Iterator<?> keys = js.keys();
                int cnt = 0;

                while (keys.hasNext()) {
                    keys.next();
                    if (cnt < js.length() -1) {
                        Node copiedMember = wpsDoc.importNode(featureMembers.item(0), true);
                        featureMembers.item(0).getParentNode().appendChild(copiedMember);
                    }
                    cnt++;
                }


            }

            featureMembers = wpsDoc.getElementsByTagName("gml:featureMember");

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
                Node feature = null;
                for (int j = 0; j < meh.getLength(); j++) {
                    if (meh.item(j).getNodeName().indexOf("feature:") == 0) {
                        feature = meh.item(j);
                        break;
                    }
                }
                // Put properties to result featureset in predefined order
                String currow = i < rowOrder.size() ? rowOrder.get(i) : null;

                Iterator<?> keys = js.keys();

                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    if (js.get(key) instanceof JSONObject) {
                        if (currow != null && key.equals(currow)) {
                            Node elem = feature.getOwnerDocument().createElement("feature:___");
                            elem.setTextContent(key);
                            feature.appendChild(elem);


                            JSONObject subjs = js.getJSONObject(key);
                            Iterator<?> subkeys = subjs.keys();
                            while (subkeys.hasNext()) {
                                String subkey = (String) subkeys.next();
                                Node subelem = feature.getOwnerDocument().createElement("feature:" + subkey.replace(" ", "_"));
                                subelem.setTextContent(subjs.get(subkey).toString());
                                colvalues.put(subkey, subelem);

                            }
                            for (int j = 0; j < colvalues.size(); j++) {
                                if(colvalues.get(colOrder.get(j)) != null) {
                                    feature.appendChild(colvalues.get(colOrder.get(j)));
                                }
                            }

                        }

                    }
                }

            }
        } catch (JSONException e) {
            log.debug("Aggregate feature property insertion failed", e);
        }
        return this.getStringFromDocument(wpsDoc);
    }

    //method to convert Document to String
    public String getStringFromDocument(Document doc)
    {
        try
        {
            Transformer transformer = XmlHelper.newTransformerFactory().newTransformer();
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);
            return result.getWriter().toString();
        }
        catch(TransformerException ex)
        {
           // ex.printStackTrace();
            return null;
        }
    }
    public void buildWfsInsertElement(StringBuilder sb, Node geometry, String geomcol, List<String> textFeatures, List<Double> numericFeatures, String uuid, long analysis_id ) throws ServiceException
    {
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
}
