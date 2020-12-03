package fi.nls.oskari.map.analysis.service;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.mutable.MutableInt;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.XmlHelper;

public class TransformationService {

    // "created" and "updated" added here to prevent them from appearing on the
    // created analysis
    private static final List<String> HIDDEN_FIELDS = Arrays.asList("analysis_id", "created", "updated");
    private static final String NUMERIC_FIELD_TYPE = "numeric";

    private static final Logger log = LogFactory.getLogger(TransformationService.class);
    private static final String WFSTTEMPLATESTART = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<wfs:Transaction xmlns:wfs=\"http://www.opengis.net/wfs\" service=\"WFS\" version=\"1.1.0\" xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:cgf=\"http://www.opengis.net/cite/geometry\" >\n";

    private static final String WFSTTEMPLATEEND = "</wfs:Transaction>\n";

    private static final String WFSTINSERTSTART = "   <wfs:Insert>\n"
            + "       <feature:analysis_data xmlns:feature=\"http://www.oskari.org\">\n";

    private static final String WFSTINSERTEND = "       </feature:analysis_data>\n" + "   </wfs:Insert>\n";

    private static final String FEATURE_MEMBER_TAG_NAME = "featureMember";
    private static final String FEATURE_MEMBERS_TAG_NAME = "featureMembers";

    public String stripNamespace(final String tag) {

        String splitted[] = tag.split(":");
        if (splitted.length > 1) {
            return splitted[1];
        }
        return splitted[0];
    }

    /**
     * Copy union feature geometry to each aggregate result and add aggregate
     * results as feature properties
     *
     * @param featureSet       WPS union response
     * @param aggregateResults Aggregate results
     *                         ([attribue1:{Count:xx,Sum=yy,..},..])
     * @return
     * @throws ServiceException
     */
    public String mergePropertiesToFeatures(String featureSet, String aggregateResults, List<String> rowOrder,
            List<String> colOrder) throws ServiceException {

        // Col order values
        Map<String, Node> colvalues = new HashMap<String, Node>();
        for (String col : colOrder) {
            colvalues.put(col, null);
        }

        final Document wpsDoc = createDoc(featureSet);
        try {
            JSONObject js = new JSONObject(aggregateResults);
            NodeList featureMembers = wpsDoc.getElementsByTagNameNS("*", FEATURE_MEMBER_TAG_NAME);
            // We trust that union is in one featureMember
            // Clone 1st fea member so that there is one fea each aggregate result
            if (featureMembers.getLength() > 0) {
                Iterator<?> keys = js.keys();
                int cnt = 0;

                while (keys.hasNext()) {
                    keys.next();
                    if (cnt < js.length() - 1) {
                        Node copiedMember = wpsDoc.importNode(featureMembers.item(0), true);
                        featureMembers.item(0).getParentNode().appendChild(copiedMember);
                    }
                    cnt++;
                }

            }

            featureMembers = wpsDoc.getElementsByTagNameNS("*", FEATURE_MEMBER_TAG_NAME);

            for (int i = 0; i < featureMembers.getLength(); i++) {
                // we're only interested in featureMembers...
                Node featureMember = featureMembers.item(i);
                String nodeNameWithoutNamespace = featureMember.getNodeName()
                        .substring(featureMember.getNodeName().indexOf(":") + 1);
                if (!FEATURE_MEMBER_TAG_NAME.equals(nodeNameWithoutNamespace)) {
                    continue;
                }
                // get features, i.e. all child elements in feature namespace
                // we trust that featureMember only has one feature...
                // find the child feature...

                NodeList meh = featureMember.getChildNodes();
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
                                Node subelem = feature.getOwnerDocument()
                                        .createElement("feature:" + subkey.replace(" ", "_"));
                                subelem.setTextContent(subjs.get(subkey).toString());
                                colvalues.put(subkey, subelem);

                            }
                            for (int j = 0; j < colvalues.size(); j++) {
                                if (colvalues.get(colOrder.get(j)) != null) {
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
        return getStringFromDocument(wpsDoc);
    }

    public String wpsFeatureCollectionToWfst(final String wps, String uuid, long analysis_id, List<String> fields,
            Map<String, String> fieldTypes, String geometryProperty, String ns_prefix) throws ServiceException {

        final Document wpsDoc = createDoc(wps);
        StringBuilder sb = new StringBuilder(WFSTTEMPLATESTART);

        Boolean membersCase = isMembersCase(wpsDoc);
        NodeList featureMembers = getFeatureMembers(wpsDoc, membersCase);

        handleFeatureMembers(uuid, analysis_id, fields, fieldTypes, geometryProperty, ns_prefix, sb, membersCase,
                featureMembers);

        sb.append(WFSTTEMPLATEEND);
        return sb.toString();
    }

    private void handleFeatureMembers(String uuid, long analysis_id, List<String> fields,
            Map<String, String> fieldTypes, String geometryProperty, String ns_prefix, StringBuilder sb,
            Boolean membersCase, NodeList featureMembers) throws ServiceException {

        List<String> cols = new ArrayList<String>();
        List<String> geomcols = new ArrayList<String>();
        geomcols.add(ns_prefix + ":" + geometryProperty);
        geomcols.add(ns_prefix + ":geometry"); // default geometry
        geomcols.add(ns_prefix + ":geom"); // geoserver uses geom in resultset

        MutableInt ncount = new MutableInt(0);
        MutableInt tcount = new MutableInt(0);

        for (int i = 0; i < featureMembers.getLength(); i++) {
            Node featureMember = featureMembers.item(i);
            // we're only interested in featureMembers... or features
            String nodeNameWithoutNamespace = featureMember.getNodeName()
                    .substring(featureMember.getNodeName().indexOf(":") + 1);
            if (!FEATURE_MEMBER_TAG_NAME.equals(nodeNameWithoutNamespace) && !membersCase) {
                continue;
            }
            handleFeatureMember(featureMember, uuid, analysis_id, fields, fieldTypes, ns_prefix, sb, membersCase, cols,
                    geomcols, ncount, tcount);
        }
    }

    private void handleFeatureMember(Node featureMember, String uuid, long analysis_id, List<String> fields,
            Map<String, String> fieldTypes, String ns_prefix, StringBuilder sb, Boolean membersCase, List<String> cols,
            List<String> geomcols, MutableInt ncount, MutableInt tcount) throws ServiceException {

        NodeList featureMembersChilds = featureMember.getChildNodes();
        NodeList features = null;
        if (membersCase) {
            features = featureMembersChilds;
        } else {
            features = getFeatures(ns_prefix, featureMembersChilds, features);
        }

        List<String> textFeatures = new ArrayList<String>();
        List<Double> numericFeatures = new ArrayList<Double>();
        if (ncount.intValue() == 0) {
            ncount.setValue(1);
            tcount.setValue(1);
        }

        Node geometry = null;
        String geomcol = "";
        for (int j = 0; j < features.getLength(); j++) {
            Node feature = features.item(j);

            // it's a feature, check if it's geometry
            if (geomcols.contains(feature.getNodeName())) {
                // geometry, store aside for now
                geomcol = feature.getNodeName();
                geometry = feature;
            } else if (feature.getNodeName().indexOf(ns_prefix + ":") == 0) {
                // only parse 8 first text ( numeric results invalid behavior later use only
                // text)
                // TODO: fix management of Date dateTime types later
                // (excluding geometry)
                if (textFeatures.size() < 8 && numericFeatures.size() < 8 && this.isHiddenField(feature) == false) {
                    // get node value
                    String strVal = feature.getTextContent();
                    String col = this.stripNamespace(feature.getNodeName());
                    Double numericVal = this.getFieldAsNumeric(col, strVal, fieldTypes);

                    if (null != numericVal) {
                        handleNumericFeature(fields, ncount, cols, numericFeatures, numericVal, col);
                    } else {
                        handleTextFeature(fields, tcount, cols, textFeatures, strVal, col);
                    }
                }
            }
        }
        buildWfsInsertElement(sb, geometry, geomcol, textFeatures, numericFeatures, uuid, analysis_id);
    }

    /**
     * Get features, i.e. all child elements in feature namespace. We trust that
     * featureMember only has one feature find the child feature.
     * 
     * @param String   ns_prefix
     * @param NodeList meh
     * @param NodeList features
     * @return NodeList features
     */
    private NodeList getFeatures(String ns_prefix, NodeList featureMembersChilds, NodeList features) {
        for (int j = 0; j < featureMembersChilds.getLength(); j++) {
            if (featureMembersChilds.item(j).getNodeName().indexOf(ns_prefix + ":") == 0) {
                features = featureMembersChilds.item(j).getChildNodes();
                break;
            }
        }
        return features;
    }

    private NodeList getFeatureMembers(final Document wpsDoc, Boolean members_case) {
        NodeList featureMembers = wpsDoc.getElementsByTagNameNS("*", FEATURE_MEMBER_TAG_NAME);

        if (featureMembers.getLength() == 0) {
            featureMembers = wpsDoc.getElementsByTagNameNS("*", FEATURE_MEMBERS_TAG_NAME);
        }
        if (members_case) {
            // select features as featureMembers
            featureMembers = wpsDoc.getElementsByTagName(featureMembers.item(0).getFirstChild().getNodeName());
        }
        return featureMembers;
    }

    private Boolean isMembersCase(final Document wpsDoc) {
        Boolean members_case = wpsDoc.getElementsByTagNameNS("*", FEATURE_MEMBER_TAG_NAME).getLength() == 0
                && wpsDoc.getElementsByTagNameNS("*", FEATURE_MEMBERS_TAG_NAME).getLength() > 0;
        return members_case;
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
            dbf.setNamespaceAware(true);
            final DocumentBuilder builder = dbf.newDocumentBuilder();
            final Document wpsDoc = builder.parse(new InputSource(new ByteArrayInputStream(content.getBytes("UTF-8"))));
            return wpsDoc;
        } catch (Exception ex) {
            throw new ServiceException("Unable to create XML doc from content");
        }
    }

    private boolean isHiddenField(Node feature) {
        String[] acol = feature.getNodeName().split(":");
        if (acol.length > 1)
            return HIDDEN_FIELDS.contains(acol[1]);

        return false;
    }

    /**
     *
     * @param fieldName
     * @param fieldTypes field types like in WFS DescribeFeatureType
     * @return true, if numeric value (int,double,long,..)
     */
    private Double getFieldAsNumeric(String fieldName, String strVal, Map<String, String> fieldTypes) {
        Double numericValue = null;
        if (fieldTypes.containsKey(fieldName)) {
            // Check type
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

    // method to convert Document to String
    public String getStringFromDocument(Document doc) {
        try {
            Transformer transformer = XmlHelper.newTransformerFactory().newTransformer();
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);
            return result.getWriter().toString();
        } catch (TransformerException ex) {
            // ex.printStackTrace();
            return null;
        }
    }

    private void handleNumericFeature(List<String> fields, MutableInt ncount, List<String> cols,
            List<Double> numericFeatures, Double dblVal, String col) {

        numericFeatures.add(dblVal);
        if (!cols.contains(col)) {
            String colmap = "n" + ncount.toString() + "=" + col;
            cols.add(col);
            fields.add(colmap);
            ncount.increment();
        }
    }

    private void handleTextFeature(List<String> fields, MutableInt tcount, List<String> cols, List<String> textFeatures,
            String strVal, String col) {

        textFeatures.add(strVal);
        if (!cols.contains(col)) {
            String colmap = "t" + tcount.toString() + "=" + col;
            cols.add(col);
            fields.add(colmap);
            tcount.increment();
        }
    }

    private void buildWfsInsertElement(StringBuilder sb, Node geometry, String geomcol, List<String> textFeatures,
            List<Double> numericFeatures, String uuid, long analysis_id) throws ServiceException {
        // build wfs:Insert element
        sb.append(WFSTINSERTSTART);
        // add geometry node
        String g = nodeToString(geometry);
        // change geom tag to geometry
        g = g.replace(geomcol, "gml:geometry");
        // change namespace of geometry's child elements from feature to gml
        g = g.replace("feature:", "gml:");
        sb.append(g);
        // add text feature nodes (1-based)
        for (int j = 0; j < textFeatures.size(); j++) {
            String feature = "         <feature:t" + (j + 1) + ">" + formatStringValue(textFeatures.get(j))
                    + "</feature:t" + (j + 1) + ">\n";
            sb.append(feature);
        }
        // add numeric feature nodes (1-based)
        for (int j = 0; j < numericFeatures.size(); j++) {
            sb.append("         <feature:n" + (j + 1) + ">" + numericFeatures.get(j) + "</feature:n" + (j + 1) + ">\n");
        }

        sb.append("         <feature:analysis_id>" + Long.toString(analysis_id) + "</feature:analysis_id>\n");

        sb.append("         <feature:uuid>" + uuid + "</feature:uuid>\n");

        sb.append(WFSTINSERTEND);
    }

    private String formatStringValue(String s) {
        s = removeLineBreaks(s);
        return replaceMultipleSpacesWithSingleSpace(s);
    }

    private String removeLineBreaks(String s) {
        return s == null ? null : s.replaceAll("\\r\\n|\\r|\\n", "");
    }

    private String replaceMultipleSpacesWithSingleSpace(String s) {
        return s == null ? null : s.trim().replaceAll("\\s{2,}", " ");
    }
}
