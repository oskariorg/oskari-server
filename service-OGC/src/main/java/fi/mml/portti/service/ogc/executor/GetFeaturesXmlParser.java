package fi.mml.portti.service.ogc.executor;

import fi.mml.portti.domain.ogc.util.CachingSchemaLocator;
import fi.mml.portti.domain.ogc.util.http.HttpPostResponse;
import fi.mml.portti.service.ogc.OgcFlowException;
import fi.nls.oskari.domain.map.wfs.FeatureParameter;
import fi.nls.oskari.domain.map.wfs.FeatureType;
import org.eclipse.xsd.util.XSDSchemaLocator;
import org.geotools.feature.FeatureCollection;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xml.Parser;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.picocontainer.MutablePicoContainer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.namespace.QName;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.*;


public class GetFeaturesXmlParser {

    public static FeatureCollection<SimpleFeatureType, SimpleFeature> parseFeatures(HttpPostResponse response, final FeatureType featureType) throws OgcFlowException {
        try {
            final boolean useProxy = featureType.getWfsService().isUseProxy();
            GMLConfiguration gml = new GMLConfiguration() {

                @Override
                public void configureContext(final MutablePicoContainer container) {
                    super.configureContext(container);
                    String username = featureType.getWfsService().getUsername();
                    String password = featureType.getWfsService().getPassword();
                    XSDSchemaLocator locator = new CachingSchemaLocator(username, password, useProxy);
                    QName key = new QName("portti", "schemaLocator");
                    container.registerComponentInstance(key, locator);
                }
            };


            Parser parser = new Parser(gml);
            parser.setValidating(false);
            parser.setFailOnValidationError(false);
            parser.setStrict(false);

            FeatureCollection<SimpleFeatureType, SimpleFeature> features = null;
            Object parseResult = parser.parse(response.getResponseAsInputStream());

            if (parseResult instanceof FeatureCollection) {
                features = (FeatureCollection<SimpleFeatureType, SimpleFeature>) parseResult;
            } else {
                throw new RuntimeException("We are trying to cast GeoTools parse results to FeatureCollection but it seems that GeoTools " +
                        "has returned data in '" + parseResult.getClass().getName() + "'. This is a problem. This can be due to a fact that " +
                        "GeoTools is not able to access XSD:s for WFS service. " +
                        "Check this discussion: 'http://osgeo-org.1803224.n2.nabble.com/WFS-MultiSurfaces-are-returned-inside-Hashmap-instead-of-FeatureCollection-td6283266.html'");
            }

            return features;
        } catch (Exception e) {
            throw new OgcFlowException("Failed to parse Features for FeatureType '" + featureType +
                    "' This server probably returned a response that could not be converted." +
                    "It might be that you are trying to do too large query.", e);

        }
    }

    public static JSONObject parseFeatures2Json(HttpPostResponse response, final FeatureType featureType) throws OgcFlowException {
        try {

            List<Map<String, List<String>>> featuresList = saxParser(response.getResponseAsInputStream(), featureType);
            int rowIdentifier = 0;

            JSONObject featureJson = new JSONObject();
            featureJson.put("uiProvider", "col");
            featureJson.put("iconCls", "task-folder");

            for (Map.Entry<String, String> title : featureType.getTitles().entrySet()) {
                featureJson.put("feature_" + title.getKey(), title.getValue());
            }

            for (Map<String, List<String>> ls : featuresList) {
                JSONObject json = new JSONObject();
                Iterator<String> feature = ls.keySet().iterator();
                while (feature.hasNext()) {
                    String key = feature.next();
                    List<String> valueList = ls.get(key);
                    if (valueList.size() > 1) {
                        handleManyValuesJson(valueList, rowIdentifier++, key, json);
                    } else {
                        if (valueList.get(0) != null) {
                            json.put(key, valueList.get(0));
                        }
                    }
                }
                if (featuresList.size() > 1) {
                    featureJson.accumulate("children", json);
                } else {
                    featureJson.append("children", json);
                }
            }

            return featureJson;
        } catch (Exception e) {
            throw new OgcFlowException("Failed to parse Features for FeatureType '" + featureType +
                    "' This server probably returned a response that could not be converted." +
                    "It might be that you are trying to do too large query.", e);
        }
    }

    private final static String GML_ID = "gml:id";
    private final static String GML_FEATURE_MEMBER = "gml:featureMember";
    private final static String GML_FEATURE_MEMBERS = "gml:featureMembers";

    // TODO: return list from featureParameters
    protected static List<Map<String, List<String>>> saxParser(InputStream inputSource, final FeatureType featureType) {
        final List<Map<String, List<String>>> features = new ArrayList<Map<String, List<String>>>();


        final List<FeatureParameter> featureParameters = new ArrayList<FeatureParameter>();

        final List<String> problemElements = new ArrayList<String>();

        problemElements.add("pnr:nimi");
        problemElements.add("ktjkiiwfs:kiinteistorajanTietoja");
        problemElements.add("ktjkiiwfs:rajamerkinTietoja");

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            DefaultHandler handler = new DefaultHandler() {
                int currentLevel = 0;
                String lastQname = "";
                int childCount = 0;
                int memberLvl = 0;
                List<String> qNames = new ArrayList<String>();

                boolean featureMember = false;
                boolean featureMembers = false;

                Map<String, List<String>> childMap = new HashMap<String, List<String>>();

                String featureMemberName = GML_FEATURE_MEMBER;

		    /* Construct basic objects */

                public void startElement(String uri, String localName, String qName, Attributes attributes)
                        throws SAXException {

                    if (attributes.getValue(GML_ID) != null) {
                        String featureId = attributes.getValue(GML_ID);
                        if (!childMap.containsKey("featureId")) {
                            List<String> valueList = new ArrayList<String>();
                            valueList.add(featureId);
                            childMap.put("featureId", valueList);

                            valueList = new ArrayList<String>();
                            valueList.add(featureType.getQname().toString());

                            childMap.put("qName", valueList);

                        }
                    }

                    qNames.add(qName);

                    currentLevel++;


                    if (featureMembers) {

                        // get first for name
                        featureMemberName = qName;
                        featureMembers = false;

                    }

                    if (!featureMembers && qName.equals(GML_FEATURE_MEMBERS)) {
                        featureMembers = true;

                    }


                    if (qName.equals(featureMemberName)) {
                        childMap = new HashMap<String, List<String>>();
                        //memberLvl = currentLevel+1;
                        memberLvl = currentLevel + 1;
                        featureMember = true;

                        if (attributes.getValue(GML_ID) != null) {
                            memberLvl = currentLevel;
                            String featureId = attributes.getValue(GML_ID);
                            List<String> valueList = new ArrayList<String>();
                            valueList.add(featureId);
                            childMap.put("featureId", valueList);
                            valueList = new ArrayList<String>();
                            valueList.add(featureType.getQname().toString());

                            childMap.put("qName", valueList);
                        }

                    }
                    lastQname = qName;
                }

                public void characters(char ch[], int start, int length) throws SAXException {

                    String value = new String(ch, start, length);
                    if (value != null) {
                        value = value.trim();
                    }

                    if (featureMember && length > 0 && !"".equals(value) && !"\n".equals(value) && !"\r".equals(value)) {

                        if (!lastQname.startsWith("gml:")) {
                            String name = "";
                            for (int i = memberLvl; i < qNames.size(); i++) {
                                name += "/" + qNames.get(i).substring(qNames.get(i).indexOf(":") + 1);
                            }

                            if (qNames.size() > memberLvl + 1 && problemElements.contains(qNames.get(3))) {

                                name = qNames.get(memberLvl + 1);
                                name = name.replace(':', '_');
                                if (childMap.containsKey(name)) {

                                    if (value != null && "".equals(value)) {
                                        List<String> valueList = new ArrayList<String>();
                                        valueList.add(childMap.get(name).get(0) + value);
                                        childMap.put(name, valueList);

                                    } else {
                                        List<String> valueList = childMap.get(name);
                                        valueList.add(qNames.get(memberLvl + 2) + "===" + value);
                                        childMap.put(name, valueList);
                                    }
                                } else {
                                    List<String> valueList = new ArrayList<String>();
                                    valueList.add(qNames.get(memberLvl + 2) + "===" + value);
                                    childMap.put(name, valueList);
                                }

                            } else {
                                name = name.substring(1);

                                if (childMap.containsKey(name)) {
                                    List<String> valueList = childMap.get(name);
                                    valueList.set(0, valueList.get(0) + value);
                                    childMap.put(name, valueList);
                                } else {
                                    List<String> valueList = new ArrayList<String>();
                                    valueList.add(value);
                                    childMap.put(name, valueList);
                                }
                            }
                        }
                    }
                }

                @Override
                public void endElement(String uri, String localName,
                                       String qName) throws SAXException {

                    if (qName.equals(featureMemberName)) {
                        features.add(childMap);
                        childCount++;
                    }
                    currentLevel--;

                    qNames.remove(currentLevel);
                    super.endElement(uri, localName, qName);
                }
            };

            saxParser.parse(inputSource, handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return features;
    }


    private static void handleManyValues(List<String> valueList, int rowIdentifier, String key, JSONObject json) throws JSONException {

        Iterator<String> values = valueList.iterator();
        rowIdentifier++;
        String subRows =
                "<div class=\"column-expander_" + rowIdentifier + "\" style=\"width: 18px; " +
                        "background-image:url('/portti-map-framework-js-portlet/map-application-framework/lib/ext-3.3.1/resources/images/gray/grid/row-expand-sprite.gif'); background-position: 4px 0px;\"" +
                        "onclick=\"jQuery('.rowIdClass_" + rowIdentifier + "').show();" +
                        "jQuery('.column-expander_" + rowIdentifier + "').hide();" +
                        "jQuery('.column-collapser_" + rowIdentifier + "').show();" +
                        "\">" +
                        "&nbsp;</div>" +

                        "<div class=\"column-collapser_" + rowIdentifier + "\" style=\"display: none; " +
                        "background-image:url('/portti-map-framework-js-portlet/map-application-framework/lib/ext-3.3.1/resources/images/gray/grid/row-expand-sprite.gif'); background-position: -21px 0px; width: 18px;\" " +
                        "onclick=\"jQuery('.rowIdClass_" + rowIdentifier + "').hide();" +
                        "jQuery('.column-expander_" + rowIdentifier + "').show();" +
                        "jQuery('.column-collapser_" + rowIdentifier + "').hide();" +
                        "\">" +
                        "&nbsp;</div>" +

                        "<div style=\"float: left; padding-left: 20px; display: none; \" class=\"even rowIdClass_" + rowIdentifier + "\"><table style=\"table-layout: auto;\" class=\"mmlTable\" style =\"width: 30%\";><tbody>";

        HashMap<String, String> rows = new HashMap<String, String>();
        while (values.hasNext()) {
            String[] value = values.next().split("===");
            String rowKey = value[0];

            if (rows.containsKey(rowKey)) {
                rows.put(rowKey, rows.get(rowKey) + "<td class=\"key\">" + value[1] + "</td>");
            } else if (value.length < 2) {
                rows.put(rowKey, "<td class=\"key\">" + rowKey + "</td>" + "<td class=\"key\"></td>");
            } else if (value[1] != null) {
                rows.put(rowKey, "<td class=\"key\">" + rowKey + "</td>" + "<td class=\"key\">" + value[1] + "</td>");
            }


        }
        Iterator<String> iter = rows.keySet().iterator();

        while (iter.hasNext()) {
            subRows += "<tr>" + rows.get(iter.next()) + "</tr>";
        }

        subRows += "</tbody></table>";

        json.put(key, subRows);

    }

    private static void handleManyValuesJson(List<String> valueList, int rowIdentifier, String key, JSONObject json) throws JSONException {

        Iterator<String> values = valueList.iterator();

        JSONObject subGrid = new JSONObject();

        while (values.hasNext()) {
            String[] value = values.next().split("===");
            String rowKey = value[0];
            subGrid.accumulate(rowKey, value[1]);
        }


        json.put(key, subGrid);

    }


}
