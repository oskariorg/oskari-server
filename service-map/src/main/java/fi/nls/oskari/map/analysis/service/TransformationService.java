package fi.nls.oskari.map.analysis.service;

import fi.nls.oskari.map.analysis.domain.AnalysisLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
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
import java.util.*;

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
            List<String> fields, Map<String,String> fieldTypes, String geometryProperty)
            throws ServiceException {

        final Document wpsDoc = createDoc(wps);
        StringBuilder sb = new StringBuilder(WFSTTEMPLATESTART);
        List<String> cols = new ArrayList<String>();
        List<String> geomcols = new ArrayList<String>();
        geomcols.add("feature:" + geometryProperty);
        geomcols.add("feature:geometry");

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
    private Double getFieldAsNumeric(String fieldName, String strVal, Map<String,String> fieldTypes)
    {
        Double numericValue = null;
       if(fieldTypes.containsKey(fieldName))
       {
           //Check type
           if(fieldTypes.get(fieldName).equals(NUMERIC_FIELD_TYPE))
           {
               try {
                   numericValue = Double.parseDouble(strVal);
               } catch (NumberFormatException nfe) {
                   // ignore
               }
           }
       }
        return numericValue;
    }

    public String addPropertiesTo1stFeature(String featureSet, String analysisLayer) throws ServiceException

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

                JSONObject js = new JSONObject(analysisLayer);
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
                        Iterator<?> skeys = subjs.keys();
                        while (skeys.hasNext()) {
                            String skey = (String) skeys.next();
                            Node elem = feature.getOwnerDocument().createElement("feature:" + skey);
                            elem.setTextContent(subjs.get(skey).toString());
                            feature.appendChild(elem);
                        }
                    } else {
                        Node elem = feature.getOwnerDocument().createElement("feature:" + key);
                        elem.setTextContent(js.getString(key));
                        feature.appendChild(elem);

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
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
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
}
