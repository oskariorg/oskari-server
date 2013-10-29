package fi.mml.portti.service.ogc.executor;

import static net.opengis.wfs.ResultTypeType.HITS_LITERAL;
import static net.opengis.wfs.ResultTypeType.RESULTS_LITERAL;
import static org.geotools.data.wfs.protocol.wfs.GetFeature.ResultType.RESULTS;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.util.IOHelper;
import net.opengis.wfs.GetFeatureType;
import net.opengis.wfs.QueryType;
import net.opengis.wfs.WfsFactory;

import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;
import org.geotools.data.wfs.protocol.wfs.GetFeature;
import org.geotools.data.wfs.protocol.wfs.GetFeature.ResultType;
import org.geotools.data.wfs.v1_1_0.GetFeatureQueryAdapter;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.wfs.v1_1.WFS;
import org.geotools.wfs.v1_1.WFSConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Encoder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;

import com.ibatis.common.logging.Log;

import fi.mml.portti.domain.ogc.util.http.EasyHttpClient;
import fi.mml.portti.domain.ogc.util.http.HttpPostResponse;
import fi.nls.oskari.domain.map.wfs.FeatureType;
import fi.nls.oskari.domain.map.wfs.SelectedFeatureType;
import fi.nls.oskari.log.Logger;

@SuppressWarnings("deprecation")
public class GetFeaturesWorker implements Callable<WFSResponseCapsule> {
	
	private static Logger log = LogFactory.getLogger(GetFeaturesWorker.class);
	public static final String PARSER_TYPE_JSON_OBJECT = "PARSER_TYPE_JSON_OBJECT";
	public static final String PARSER_TYPE_XML = "PARSER_TYPE_XML";
	public static final String PARSER_TYPE_FEATURES = "PARSER_TYPE_FEATURES";
	
	private SelectedFeatureType selectedFeatureType;
	
	private Filter filter;
	
	private String parserType;
	
	private boolean fetchAllProperties;
	
	public GetFeaturesWorker(
			String parserType, 
			SelectedFeatureType selectedFeatureType, 
			Filter filter,
			boolean fetchAllProperties) {
		this.parserType = parserType;
		this.selectedFeatureType = selectedFeatureType;
		this.filter = filter;
		this.fetchAllProperties = fetchAllProperties;
	}
	
	/**
	 * Constructs a new worker
	 * 
	 * @param featureType
	 * @param filter
	 * @param fetchAllProperties This is a temporary solution. In some point we will have to
	 *                           enable fetching of SelectedFeatureParameters, but for now
	 *                           we will use just a flag if all properties should be fetched
	 *                           or not.
	 */
	public GetFeaturesWorker(SelectedFeatureType selectedFeatureType, Filter filter, boolean fetchAllProperties) {
		this(PARSER_TYPE_FEATURES, selectedFeatureType, filter, fetchAllProperties);
	}
	
	public WFSResponseCapsule call() throws Exception {
		
		FeatureType featureType = selectedFeatureType.getFeatureType();
		
        /* Add properities */
        List<String> props = new ArrayList<String>();
        
        /* This will be replaced with SelectedFeatureTypes in some point */
        if (!fetchAllProperties) {
        	props.add(featureType.getBboxParameterName());
        }
        
        String[] propNames = props.toArray(new String[props.size()]);

        /* Create Query Filter */        
        Query query = new DefaultQuery(featureType.getQname().getPrefix() + ":" + featureType.getQname().getLocalPart(), filter, propNames);
        query.setMaxFeatures(selectedFeatureType.getMaxNumDisplayedItems());
        
        /* Create GetFeature */
        String gmlVersion = featureType.getWfsService().getGmlVersion();
        String srsName = "EPSG:3067";
        GetFeature gf = new GetFeatureQueryAdapter(query, "text/xml; subtype=gml/" + gmlVersion, srsName, RESULTS);
        GetFeatureType gft = createGetFeatureRequest(gf);

        /* Create Encoder */
        Configuration wfs_1_1_0_Configuration = new WFSConfiguration();
        Encoder encoder = new Encoder(wfs_1_1_0_Configuration);
        final Charset charset = Charset.forName("UTF-8");
        encoder.setEncoding(charset);
        
        /* Send Request and read response */
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        encoder.encode(gft, WFS.GetFeature, baos);
        String payload = baos.toString();

        //# This is dirty fix cause bug on Arc 9.3 server
        if (featureType.getWfsService().isGml2typeSeparator()) {
        	payload = payload.replaceAll("epsg.xml#3067", "epsg.xml:3067");
        }
        // FIXME: this is generated in front of actual srsname, we need to remove it since servers can handle it
        // maybe find a better way to deal with it
        payload = payload.replaceAll("urn:x-ogc:def:crs:", "");

        String url = featureType.getWfsService().getUrl();
        String username = featureType.getWfsService().getUsername(); 
        String password = featureType.getWfsService().getPassword();
        //boolean useProxy = featureType.getWfsService().isUseProxy();
        // TODO: refactor whole "easy http client" out of here
        HttpPostResponse response = EasyHttpClient.post(url, username, password, payload);
        
        if (!response.wasSuccessful()) {
        throw new RuntimeException("Failed to perform query to url '" + url + "'\n" +
        		"Message was: \n" + payload + "\n" + 
        		"Reponse was: \n" + response.getResponseAsString());
        }     
        
        /* Convert results to Features */
        WFSResponseCapsule wfsResponseCapsule = new WFSResponseCapsule();

	// System.err.println("---------");
	// System.err.println(response.getResponseAsString());
	// System.err.println("---------");
        
        try {
        	if (PARSER_TYPE_FEATURES.equals(parserType)) {
        		wfsResponseCapsule.setFeatures(GetFeaturesXmlParser.parseFeatures(response, featureType));
        	} else if(PARSER_TYPE_XML.equals(parserType)) {
        		String xmlResponse = response.getResponseAsString();
        		wfsResponseCapsule.setXmlData(xmlResponse);
        	} else {
        		wfsResponseCapsule.setJsonObject(GetFeaturesXmlParser.parseFeatures2Json(response, featureType));
        	}
        } finally {
        	response.closeResponseStream();
        }
        
        return wfsResponseCapsule;
    }

	@SuppressWarnings("unchecked")
	public GetFeatureType createGetFeatureRequest(GetFeature query) throws IOException {
        final WfsFactory factory = WfsFactory.eINSTANCE;

        GetFeatureType getFeature = factory.createGetFeatureType();
        getFeature.setService("WFS");
        getFeature.setVersion("1.1.0");
        getFeature.setOutputFormat(query.getOutputFormat());

        getFeature.setHandle("GeoTools " + GeoTools.getVersion() + " WFS DataStore");
        Integer maxFeatures = query.getMaxFeatures();
        if (maxFeatures != null) {
        	getFeature.setMaxFeatures(BigInteger.valueOf(maxFeatures.intValue()));
        }

        ResultType resultType = query.getResultType();
        getFeature.setResultType(RESULTS == resultType ? RESULTS_LITERAL : HITS_LITERAL);

        QueryType wfsQuery = factory.createQueryType();
        wfsQuery.setTypeName(Collections.singletonList(query.getTypeName()));

        Filter serverFilter = query.getFilter();
        if (!Filter.INCLUDE.equals(serverFilter)) {
        	wfsQuery.setFilter(serverFilter);
        }
        
        String srsName = query.getSrsName();
        try {
        	wfsQuery.setSrsName(new URI(srsName));
        } catch (URISyntaxException e) {
        	throw new RuntimeException("Can't create a URI from the query CRS: " + srsName, e);
        }
        
        String[] propertyNames = query.getPropertyNames();
        boolean retrieveAllProperties = propertyNames == null;
        
        if (!retrieveAllProperties) {
        	List propertyName = wfsQuery.getPropertyName();
            for (String propName : propertyNames) {
            	propertyName.add(propName);
            }
        }
        
        SortBy[] sortByList = query.getSortBy();
        if (sortByList != null) {
            for (SortBy sortBy : sortByList) {
            	wfsQuery.getSortBy().add(sortBy);
            }
        }

        getFeature.getQuery().add(wfsQuery);
        return getFeature;
    }        
    
}
