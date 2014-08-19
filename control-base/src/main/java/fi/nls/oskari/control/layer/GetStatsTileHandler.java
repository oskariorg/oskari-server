package fi.nls.oskari.control.layer;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
//import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.view.GetAppSetupHandler;
import fi.nls.oskari.domain.map.stats.StatsVisualization;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.stats.VisualizationService;
import fi.nls.oskari.map.stats.VisualizationServiceIbatisImpl;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.axiom.om.OMElement;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Enumeration;

@OskariActionRoute("GetStatsTile")
public class GetStatsTileHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(GetStatsTileHandler.class);

    private final VisualizationService service = new VisualizationServiceIbatisImpl();

    final private static String PARAM_VISUALIZATION_ID = "VIS_ID";
    final private static String PARAM_VISUALIZATION_NAME = "VIS_NAME"; // name=ows:Kunnat2013
    final private static String PARAM_VISUALIZATION_FILTER_PROPERTY = "VIS_ATTR"; // attr=Kuntakoodi
    final private static String PARAM_VISUALIZATION_CLASSES = "VIS_CLASSES"; // classes=020,091|186,086,982|111,139,740
    final private static String PARAM_VISUALIZATION_VIS = "VIS_COLORS"; // vis=choro:ccffcc|99cc99|669966

    public void handleAction(final ActionParameters params)
            throws ActionException {

    	
    	String makePostRequest = PropertyUtil.get("statistics.sld.server");
    	if(makePostRequest != null){
    		log.debug("got statistics.sld.server parameter");
    	}else{
    		log.debug("statistics.sld.server parameter missing");
    	}
    	
    	
    	if(params.getHttpParam("new") != null){
    		String url = "http://dev.paikkatietoikkuna.fi/geoserver/wms?";
    		log.debug("in new method 3334");
    		
    		getSLD(params);
    		
    		try{
    			String xml = buildXML();
    			
        		HttpURLConnection con = IOHelper.getConnection(url);
        		IOHelper.writeHeader(con, "Content-type", "text/xml");
        		IOHelper.writeToConnection(con, xml);
        		byte[] presponse = IOHelper.readBytes(con);
        		
	            log.debug("presponse legth: " + presponse.length);
	            //String s = new String(presponse);
	            //log.debug("s: " + s);
        		//log.debug("presponse: " + presponse.toString());
	            // read the image tile
	
	            final HttpServletResponse response = params.getResponse();
	            response.setContentType("image/png");
	            response.getOutputStream().write(presponse, 0, presponse.length);
	            response.getOutputStream().flush();
	            response.getOutputStream().close();        		
    			
	        } catch (Exception e) {
	            throw new ActionException("Couldn't post proxy request to geoserver",
	                    e);
	        }    	
    	}else{
    		printParameters(params);
	        final HttpURLConnection con = getConnection(params);
	        try {
	        	log.debug("ssss");
	            // we should post complete GetMap XML with the custom SLD to geoserver so it doesn't need to fetch it again
	            // Check: http://geo-solutions.blogspot.fi/2012/04/dynamic-wms-styling-with-geoserver-sld.html
	            con.setRequestMethod("GET");
	
	            con.setDoOutput(false);
	            con.setDoInput(true);
	            HttpURLConnection.setFollowRedirects(false);
	            con.setUseCaches(false);
	            con.connect();
	            //IOHelper.writeToConnection(con, SLD_HANDLER.getSLD(params));
	
	            // read the image tile
	            final byte[] presponse = IOHelper.readBytes(con.getInputStream());
	            log.debug("11presponse legth: " + presponse.length);
	            //String s = new String(presponse);
	            //log.debug("xxxs: " + s);
	            
	
	            final HttpServletResponse response = params.getResponse();
	            response.setContentType("image/png");
	            response.getOutputStream().write(presponse, 0, presponse.length);
	            response.getOutputStream().flush();
	            response.getOutputStream().close();
	        } catch (Exception e) {
	            throw new ActionException("Couldn't proxy request to geoserver",
	                    e);
	        } finally {
	            if(con != null) {
	                con.disconnect();
	            }
	        }
    		
    	}
    }
    
    private HttpURLConnection getConnection(final ActionParameters params)
            throws ActionException {

    	log.debug("otetaan yhteyksia");
        // copy parameters
        final HttpServletRequest httpRequest = params.getRequest();
        final StringBuilder ajaxUrl = new StringBuilder(PropertyUtil.get("statistics.sld.server"));
        
    	log.debug("properteista haettu: " + ajaxUrl.toString());
        
        ajaxUrl.append(PropertyUtil.get("statistics.sld.server.path",
                PropertyUtil.get(params.getLocale(), GetAppSetupHandler.PROPERTY_AJAXURL)));
        
        log.debug("properteista haettu2: " + ajaxUrl.toString());
        
        ajaxUrl.append("&action_route=GetStatsLayerSLD");
        
        log.debug("muuten vaan lisatty: " + ajaxUrl.toString());

        StatsVisualization vis = getVisualization(params);
        if(vis == null) {
            log.info("Visualization couldn't be generated - parameters/db data missing", params);
        } else {
        	log.debug("rakennellaan urli ite");
            // using prefetched values so we don't need to get them from db again on SLD action
            ajaxUrl.append("&");
            ajaxUrl.append(GetStatsLayerSLDHandler.PARAM_VISUALIZATION_NAME);
            ajaxUrl.append("=");
            ajaxUrl.append(vis.getLayername());

            ajaxUrl.append("&");
            ajaxUrl.append(GetStatsLayerSLDHandler.PARAM_VISUALIZATION_FILTER_PROPERTY);
            ajaxUrl.append("=");
            ajaxUrl.append(vis.getFilterproperty());

            ajaxUrl.append("&");
            ajaxUrl.append(GetStatsLayerSLDHandler.PARAM_VISUALIZATION_CLASSES);
            ajaxUrl.append("=");
            ajaxUrl.append(vis.getClasses());

            ajaxUrl.append("&");
            ajaxUrl.append(GetStatsLayerSLDHandler.PARAM_VISUALIZATION_VIS);
            ajaxUrl.append("=");

            ajaxUrl.append(vis.getVisualization());
            ajaxUrl.append(":");
            ajaxUrl.append(vis.getColors());
        }
                
        final StringBuffer queryString = new StringBuffer();
        for (Object key : httpRequest.getParameterMap().keySet()) {
            String keyStr = (String) key;
            queryString.append("&");
            queryString.append(keyStr);
            queryString.append("=");
            queryString.append(params.getHttpParam(keyStr));
        }
        log.debug("naa on tullu requestissa: " + queryString.toString());
        try {
            final String url = PropertyUtil.get("statistics.geoserver.wms.url") + queryString + "&SLD=" + URLEncoder.encode(ajaxUrl.toString(), "UTF-8");
            log.debug("Getting stats tile from url:", url);
            return IOHelper.getConnection(url, PropertyUtil.get("statistics.user"), PropertyUtil.get("statistics.password"));
        } catch (Exception e) {
            throw new ActionException(
                    "Couldnt get connection to geoserver", e);
        }
    }

    private StatsVisualization getVisualization(final ActionParameters params) {
        final int visId = ConversionHelper.getInt(
                params.getHttpParam(PARAM_VISUALIZATION_ID), -1);
        
        log.debug("visid: " + visId);
        
        log.debug("PARAM_VISUALIZATION_CLASSES: " + params.getHttpParam(PARAM_VISUALIZATION_CLASSES));
        log.debug("PARAM_VISUALIZATION_NAME: " + params.getHttpParam(PARAM_VISUALIZATION_NAME));
        log.debug("PARAM_VISUALIZATION_FILTER_PROPERTY: " + params.getHttpParam(PARAM_VISUALIZATION_FILTER_PROPERTY));
        log.debug("PARAM_VISUALIZATION_VIS: " + params.getHttpParam(PARAM_VISUALIZATION_VIS, ""));
        
        return service.getVisualization(
                visId,
                params.getHttpParam(PARAM_VISUALIZATION_CLASSES),
                params.getHttpParam(PARAM_VISUALIZATION_NAME),
                params.getHttpParam(PARAM_VISUALIZATION_FILTER_PROPERTY),
                params.getHttpParam(PARAM_VISUALIZATION_VIS, "")
                );
        
    }
    
    /**
     * 
     * <?xml version="1.0" encoding="UTF-8"?>
	 *<ogc:GetMap xmlns:ogc="http://www.opengis.net/ows"
	 *            xmlns:gml="http://www.opengis.net/gml"
	 *   version="1.1.1" service="WMS">
 	 *  <StyledLayerDescriptor version="1.0.0">
 	 *     <NamedLayer>
   	 *     <Name>topp:states</Name>
   	 *     <NamedStyle><Name>population</Name></NamedStyle>
   	 *   </NamedLayer>
  	 * </StyledLayerDescriptor>
  	 * <BoundingBox srsName="http://www.opengis.net/gml/srs/epsg.xml#4326">
  	 *    <gml:coord><gml:X>-130</gml:X><gml:Y>24</gml:Y></gml:coord>
  	 *    <gml:coord><gml:X>-55</gml:X><gml:Y>50</gml:Y></gml:coord>
  	 * </BoundingBox>
  	 * <Output>
  	 *    <Format>image/png</Format>
 	 *     <Size><Width>550</Width><Height>250</Height></Size>
 	 *  </Output>
	 * </ogc:GetMap>
     * 
     */
    private String buildXML(){
    	Element getMapElement = null;
    	String transformedResponse = null;
    	try{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	 
			log.debug("1");
			
			// root elements
			Document doc = docBuilder.newDocument();
			getMapElement = doc.createElement("ogc:GetMap");
			
//			Attr xmlns_attr = doc.createAttribute("xmlns");
//			xmlns_attr.setValue("http://www.opengis.net/sld");
//			getMapElement.setAttributeNode(xmlns_attr);
			
			Attr xmlns_ogc_attr = doc.createAttribute("xmlns:ogc");
			xmlns_ogc_attr.setValue("http://www.opengis.net/ows");
			getMapElement.setAttributeNode(xmlns_ogc_attr);			

			Attr xmlns_gml_attr = doc.createAttribute("xmlns:gml");
			xmlns_gml_attr.setValue("http://www.opengis.net/gml");
			getMapElement.setAttributeNode(xmlns_gml_attr);
			
			Attr xversion_attr = doc.createAttribute("version");
			xversion_attr.setValue("1.1.1");
			getMapElement.setAttributeNode(xversion_attr);
			
			Attr service_attr = doc.createAttribute("service");
			service_attr.setValue("WMS");
			getMapElement.setAttributeNode(service_attr);

			doc.appendChild(getMapElement);
	 
			// styledLayerDescription elements
			Element styledLayerDescription = doc.createElement("StyledLayerDescriptor");
			
			Attr version_attr = doc.createAttribute("version");
			version_attr.setValue("1.1.1");
			styledLayerDescription.setAttributeNode(version_attr);
			getMapElement.appendChild(styledLayerDescription);
			
			// NamedLayer elements
			Element namedLayer = doc.createElement("NamedLayer");
			styledLayerDescription.appendChild(namedLayer);

			// Name element
			Element name = doc.createElement("Name");
			name.appendChild(doc.createTextNode("oskari:kunnat_plus_2013"));
			namedLayer.appendChild(name);
			
			Element namedStyle = doc.createElement("NamedStyle");
			Element name2 = doc.createElement("Name");
			name2.appendChild(doc.createTextNode("polygon"));
			namedStyle.appendChild(name2);
			namedLayer.appendChild(namedStyle);

			Element boundingBox = doc.createElement("BoundingBox");
			Attr srsName_attr = doc.createAttribute("srsName");
			srsName_attr.setValue("http://www.opengis.net/gml/srs/epsg.xml#3067");
			boundingBox.setAttributeNode(srsName_attr);
			
			
			getMapElement.appendChild(boundingBox);

			Element gml_coord = doc.createElement("gml:coord");
			boundingBox.appendChild(gml_coord);
			
			Element gml_x = doc.createElement("gml:x");
			gml_x.appendChild(doc.createTextNode("265000"));
			gml_coord.appendChild(gml_x);
			
			Element gml_y = doc.createElement("gml:y");
			gml_y.appendChild(doc.createTextNode("6624132"));
			gml_coord.appendChild(gml_y);
			
			
			Element gml_coord2 = doc.createElement("gml:coord");
			boundingBox.appendChild(gml_coord2);
			
			Element gml_x2 = doc.createElement("gml:x");
			gml_x2.appendChild(doc.createTextNode("365000"));
			gml_coord2.appendChild(gml_x2);
			
			Element gml_y2 = doc.createElement("gml:y");
			gml_y2.appendChild(doc.createTextNode("6875604"));
			gml_coord2.appendChild(gml_y2);
			
			
			Element output = doc.createElement("Output");
			getMapElement.appendChild(output);
			
			Element format = doc.createElement("Format");
			format.appendChild(doc.createTextNode("image/png"));
			output.appendChild(format);
			
			Element size = doc.createElement("Size");
			output.appendChild(size);
			
			Element width = doc.createElement("Width");
			width.appendChild(doc.createTextNode("550"));
			size.appendChild(width);
			
			Element height = doc.createElement("height");
			height.appendChild(doc.createTextNode("250"));
			size.appendChild(height);
			
	        final Transformer transformer = TransformerFactory.newInstance()
	                .newTransformer();

	        final DOMSource source = new DOMSource(doc);
	        final StringWriter outWriter = new StringWriter();
	        final StreamResult result = new StreamResult(outWriter);
	        transformer.transform(source, result);
	        transformedResponse = outWriter.toString();
	        log.debug("XMMMXMMXLLLLAAA: " + transformedResponse);
			
	        
    	}catch(Exception e){
    		log.debug("XML error");
    		e.printStackTrace();
    	}
    	return transformedResponse;
    }
    
    private String getSLD(final ActionParameters params)
            throws ActionException {
    	
    	final String PARAM_LANGUAGE = "lang";
        final String PARAM_MODE = "mode";
        final String MODE_XML = "XML";
    	
    	
    	printParameters(params);

        final String lang = params.getHttpParam(PARAM_LANGUAGE, params
                .getLocale().getLanguage());

        final boolean modeXML = MODE_XML.equals(params.getHttpParam(PARAM_MODE,
                "").toUpperCase());

        final StatsVisualization vis = getVisualization(params);
        if (vis == null) {
            throw new ActionParamsException(
                    "Couldn't get requested visualization");
        }

        log.debug("Found visualization:", vis);
        final OMElement xml = service.getXML(vis, lang);
        try {
            if (modeXML) {
                return xml.toString();
            } else {
//                return service.transform(xml, service.getDefaultXSLT());
            	String xmlString = service.transform(xml, service.getDefaultXSLT());
            	log.debug("xmlString: " + xmlString);
            	return xmlString;
            }
        } catch (Exception e) {
            throw new ActionException("Unable to create SLD", e);
        }
    }    
    
    private void printParameters(ActionParameters params){
    	Enumeration e = params.getRequest().getParameterNames();
    	
    	log.debug("print keys");
    	while(e.hasMoreElements()){
    		String key = (String)e.nextElement();
    		log.debug("Key: " +key);
    		String[] values = params.getRequest().getParameterValues(key);
    		
    		for(String value : values){
    			log.debug("  value: " + value);
    		}
    	}
    }

}