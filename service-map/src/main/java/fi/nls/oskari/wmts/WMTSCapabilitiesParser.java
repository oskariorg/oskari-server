package fi.nls.oskari.wmts;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.XmlHelper;
import org.apache.axiom.om.OMElement;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * NOTE!!! Initial scripblings for WMTS capabilities parsing. Not used anywhere and might change without notice!!
 */
public class WMTSCapabilitiesParser {

    private Logger log = LogFactory.getLogger(WMTSCapabilitiesParser.class);

    private static final String TAG_SERVICE_IDENTIFICATION = "serviceIdentification";
    private static final String TAG_SERVICE_PROVIDER = "serviceProvider";
    private static final String TAG_OPERATIONS_METADATA = "operationsMetadata";
    private static final String TAG_CONTENTS = "contents";
    private static final String TAG_VERSION = "version";

    private static final QName XLINK_HREF = new QName("http://www.w3.org/1999/xlink", "href", "xlink");

    public JSONObject parseCapabilitiesToJSON(final String xml) throws Exception {
        if(xml == null) {
            return null;
        }
        OMElement doc = XmlHelper.parseXML(xml);

        JSONObject obj = parseJSON(doc);
        JSONObject result = null;
        if(obj != null && obj.has("capabilities")) {
            result = obj.optJSONObject("capabilities");
            JSONHelper.putValue(result, TAG_VERSION, doc.getAttributeValue(QName.valueOf("version")));
        }
        if(result != null) {
            JSONObject contents = JSONHelper.getJSONObject(result, "contents");

            JSONArray layers = setupArray(contents.remove("layer"));
            JSONHelper.putValue(contents, "layers", layers);

            JSONArray tileMatrixSets = setupArray(contents.remove("tileMatrixSet"));
            JSONHelper.putValue(contents, "tileMatrixSets", tileMatrixSets);
        }

        return result;
    }

    /**
     * Wraps given parameter object to a JSONArray if it isn't one already.
     * @param obj
     * @return
     */
    private JSONArray setupArray(final Object obj) {
        if(obj instanceof JSONArray) {
            return (JSONArray) obj;
        }
        else if (obj instanceof JSONObject) {
            return JSONHelper.createJSONArray(obj);
        }
        return new JSONArray();
    }

    /*
    "title": "Maanmittauslaitoksen Karttakuvapalvelu (WMTS)",
    "abstract": "Palvelun kautta saat käyttöösi maasto- ja taustakartat, ortokuvat sekä kiinteistörajat JHS 180 - suosituksen mukaisessa karttatiilimallissa ETRS-TM35FIN-karttaprojektiossa.",
    "serviceType": {
        "codeSpace": null,
        "value": "OGC WMTS"
    },
    "serviceTypeVersion": "1.0.0",
    "accessConstraints": "Vaatii käyttäjätunnuksen"

        <ows:Title>Maanmittauslaitoksen Karttakuvapalvelu (WMTS)</ows:Title>
        <ows:Abstract>Palvelun kautta saat käyttöösi maasto- ja taustakartat, ortokuvat sekä kiinteistörajat JHS 180 - suosituksen mukaisessa karttatiilimallissa ETRS-TM35FIN-karttaprojektiossa.</ows:Abstract>
        <ows:ServiceType>OGC WMTS</ows:ServiceType>
        <ows:ServiceTypeVersion>1.0.0</ows:ServiceTypeVersion>
        <ows:AccessConstraints>Vaatii käyttäjätunnuksen</ows:AccessConstraints>
     */
    private JSONObject parseServiceIdentification(OMElement elem) {
        final JSONObject result = new JSONObject();

        Iterator it = elem.getChildElements();
        while(it.hasNext()) {
            OMElement tag = (OMElement)it.next();
            if("Title".equalsIgnoreCase(tag.getLocalName())) {
                JSONHelper.putValue(result, "title", tag.getText());
            }
            else if("Abstract".equalsIgnoreCase(tag.getLocalName())) {
                JSONHelper.putValue(result, "abstract", tag.getText());
            }
            else if("ServiceTypeVersion".equalsIgnoreCase(tag.getLocalName())) {
                JSONHelper.putValue(result, "serviceTypeVersion", tag.getText());
            }
            else if("AccessConstraints".equalsIgnoreCase(tag.getLocalName())) {
                JSONHelper.putValue(result, "accessConstraints", tag.getText());
            }
            else if("ServiceType".equalsIgnoreCase(tag.getLocalName())) {
                JSONObject type = new JSONObject();
                try {
                    type.putOpt("codeSpace", JSONObject.NULL);
                }
                catch (Exception ignored) { }
                JSONHelper.putValue(type, "value", tag.getText());
                JSONHelper.putValue(result, "serviceType", type);
            }
        }
        return result;
    }


    /*
    "providerName": "Maanmittauslaitos",
    "providerSite": "http://www.maanmittauslaitos.fi",
    "serviceContact": {
        "individualName": "Verkkopalvelut - sovellustuki",
        "contactInfo": {
            "phone": {
                "voice": "+358 29 530 1116"
            },
            "address": {
                "electronicMailAddress": "sovellustuki@maanmittauslaitos.fi"
            }
        }
    }
     */
    private JSONObject parseServiceProvider(OMElement elem) throws Exception {
        final JSONObject result = new JSONObject();

        if(true) return XML.toJSONObject(XmlHelper.toString(elem));;

        JSONHelper.putValue(result, "providerName", XmlHelper.getChildValue(elem, "ProviderName"));
        //xlink:href="http://www.maanmittauslaitos.fi"
        JSONHelper.putValue(result, "providerSite", XmlHelper.getChild(elem, "ProviderSite").getAttributeValue(XLINK_HREF));

                /*
                    <ows:IndividualName>Verkkopalvelut - sovellustuki</ows:IndividualName>
                    <ows:ContactInfo>
                        <ows:Phone>
                            <ows:Voice>+358 29 530 1116</ows:Voice>
                        </ows:Phone>
                        <ows:Address>
                            <ows:ElectronicMailAddress>sovellustuki@maanmittauslaitos.fi</ows:ElectronicMailAddress>
                        </ows:Address>
                    </ows:ContactInfo>
            */
        OMElement contactElem = XmlHelper.getChild(elem, "ServiceContact");
        JSONObject contact = new JSONObject();
        JSONHelper.putValue(result, "serviceContact", contact);
        JSONHelper.putValue(contact, "individualName", XmlHelper.getChildValue(contactElem, "IndividualName"));

        JSONObject contactInfo = new JSONObject();
        JSONHelper.putValue(contact, "contactInfo", contactInfo);
        OMElement contactInfoElem = XmlHelper.getChild(elem, "ContactInfo");

        return result;
    }

    private JSONObject parseOperationsMetadata(OMElement elem) {
        return new JSONObject();
    }
    private JSONObject parseContents(OMElement elem) {
        return new JSONObject();
    }

    public static JSONObject parseJSON(final OMElement elem) throws Exception {
        if(elem == null) {
            return null;
        }
        JSONObject obj = new JSONObject();
        parseJSON(elem, obj);
        return obj;
    }
    private static JSONObject parseJSON(final OMElement elem, final JSONObject parent) throws Exception {
        if(elem == null) {
            return null;
        }
        final String name = ConversionHelper.decapitalize(elem.getLocalName(), true);
        Iterator<OMElement> it = elem.getChildElements();
        if(!it.hasNext()) {
            final String text = elem.getText();
            // assume content node if no child elements
            if(text != null && !text.isEmpty()) {
                JSONHelper.putValue(parent, name, text);
            }
            else {
                String href = XmlHelper.getAttributeValue(elem, "href");
                JSONHelper.putValue(parent, name, href);
            }
        }
        else {
            final String nameAttr = XmlHelper.getAttributeValue(elem, "name");
            JSONObject obj = new JSONObject();
            if(nameAttr != null) {
                JSONHelper.accumulateValue(parent, nameAttr, obj);
            } else {
                JSONHelper.accumulateValue(parent, name, obj);
            }
            while(it.hasNext()) {
                parseJSON(it.next(), obj);
            }
        }
        return parent;
    }
}
