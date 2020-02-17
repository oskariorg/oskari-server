package fi.nls.oskari.wfs;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xml.Parser;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author SMAKINEN
 */
public class WFSCapabilitiesParser {

    private static final Logger log = LogFactory.getLogger(WFSCapabilitiesParser.class);
    private final Object EMPTY_OBJECT = new Object();
    private final String[] EMPTY_RESULT = new String[0];
    private final String locale = PropertyUtil.getDefaultLanguage();

    public String[] getKeywordsForLayer(OskariLayer layer) throws ServiceException {
        if(!OskariLayer.TYPE_WFS.equals(layer.getType())) {
            return EMPTY_RESULT;
        }
        final GMLConfiguration configuration = new GMLConfiguration();
        final Parser parser = new Parser(configuration);
        parser.setValidating(false);
        parser.setFailOnValidationError(false);
        parser.setStrict(false);

        final Map response;
        try {

            Map<String, String> params = new HashMap<>();
            params.put("service", "WFS");
            params.put("request", "GetCapabilities");
            params.put("version", layer.getVersion());

            final HttpURLConnection con = IOHelper.getConnection(IOHelper.constructUrl(layer.getUrl(), params),
                    layer.getUsername(), layer.getPassword());

            response = (Map) parser.parse(con.getInputStream());
        } catch (Exception ex) {
            throw new ServiceException("Error getting capabilities for layer", ex);
        }

        //log.debug("WFS GetCapabilities\r\n", response.keySet());
        final Object featureTypes = safeTraverseParsedObjectAsMap(response, "FeatureTypeList.FeatureType");

        if(featureTypes != null && featureTypes instanceof List) {
            final List list = (List) featureTypes;
            //log.debug("WFS GetCapabilities.featureTypes\r\n", list);
            List<String> resultKeywords = new ArrayList<String>();
            for(Object o : list) {
                final Object name = safeTraverseParsedObjectAsMap(o, "Name");
                //log.debug("FeatureType name:", name, "should match:", conf.getLayerName());
                if(layer.getName() == null || !layer.getName().equals(name)) {
                    // not this layer
                    continue;
                }
                final Object keyword = safeTraverseParsedObjectAsMap(o, "Keywords.Keyword");
                //log.debug("WFS GetCapabilities.keywords\r\n", keyword);
                if(keyword != null && keyword instanceof List) {
                    for(Object word : (List) keyword) {
                        resultKeywords.add(word.toString());
                    }
                }

            }
            log.debug("Keywords for WFS:", layer.getName(locale), "- list:", resultKeywords);
            return resultKeywords.toArray(EMPTY_RESULT);
        }
        return EMPTY_RESULT;
    }

    private Object safeTraverseParsedObjectAsMap(final Object obj, final String key) {
        //log.debug("Traversing " + key);
        final String[] path = key.split("\\.");
        if(obj instanceof Map) {
            final Map map = (Map) obj;
            if(map.containsKey(path[0])) {
                final Object value = map.get(path[0]);
                if(path.length == 1) {
                    //log.debug("FOUND!", value);
                    return value;
                }
                else {
                    return safeTraverseParsedObjectAsMap(value, key.substring(key.indexOf('.') + 1));
                }
            }
            //log.debug("Couldnt find ", path[0], "from", map);
        }
        return EMPTY_OBJECT;
    }
}
