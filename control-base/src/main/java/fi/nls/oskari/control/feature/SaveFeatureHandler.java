package fi.nls.oskari.control.feature;

import java.io.IOException;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.db.DBHandler;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.data.domain.OskariLayerResource;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.permission.domain.Resource;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.wfs.WFSLayerConfigurationService;
import fi.nls.oskari.wfs.WFSLayerConfigurationServiceIbatisImpl;

@OskariActionRoute("SaveFeature")
public class SaveFeatureHandler extends ActionHandler {
	private OskariLayerService layerService;
	private PermissionsService permissionsService;
	private WFSLayerConfigurationService layerConfigurationService;
	private String geometryProperty;
	private static Logger log = LogFactory.getLogger(DBHandler.class);
	
	public final static String KEY = "WFSImage_";
	
	@Override
	public void handleAction(ActionParameters params) throws ActionException {
		layerService = new OskariLayerServiceIbatisImpl();
		permissionsService = new PermissionsServiceIbatisImpl();
		layerConfigurationService = new WFSLayerConfigurationServiceIbatisImpl();
		String featureData = params.getHttpParam("featureData");

		try {
			JSONObject jsonObject = new JSONObject(featureData);
			OskariLayer lay = layerService.find(jsonObject.getString("layerId"));
			WFSLayerConfiguration lc = layerConfigurationService.findConfiguration(lay.getId());
			String url = lc.getURL();
			final String user = lc.getUsername();
			final String pass = lc.getPassword();
			geometryProperty = lc.getGMLGeometryProperty();
			final Resource resource = permissionsService.findResource(new OskariLayerResource(lay));
            final boolean hasPermssion = resource.hasPermission(params.getUser(), Permissions.PERMISSION_TYPE_EDIT_LAYER_CONTENT);
            if(hasPermssion) {
            	ClearLayerTiles(lay.getId());
				StringBuilder requestData = new StringBuilder("<wfs:Transaction service='WFS' version='1.1.0' xmlns:ogc='http://www.opengis.net/ogc' xmlns:wfs='http://www.opengis.net/wfs'><wfs:Update typeName='"+ lay.getName() +"'>");
				JSONArray jsonArray = jsonObject.getJSONArray("featureFields");
				for (int i = 0; i < jsonArray.length(); i++) {
					 	requestData.append("<wfs:Property><wfs:Name>" + jsonArray.getJSONObject(i).getString("key") + "</wfs:Name><wfs:Value>" + jsonArray.getJSONObject(i).getString("value") + "</wfs:Value></wfs:Property>");
				}
				
				if (jsonObject.has("geometries")) {
					FillGeometries(requestData, jsonObject.getJSONObject("geometries"));
				};
				requestData.append("<ogc:Filter><ogc:FeatureId fid='" + jsonObject.getString("featureId") + "'/></ogc:Filter></wfs:Update></wfs:Transaction>");
				
				HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
				Credentials credentials = new UsernamePasswordCredentials(user, pass);
				CredentialsProvider credsProvider = new BasicCredentialsProvider();
				credsProvider.setCredentials( AuthScope.ANY, credentials);
				
				httpClientBuilder.setDefaultCredentialsProvider(credsProvider);
				HttpClient httpClient = httpClientBuilder.build();
				HttpPost request = new HttpPost(url);			
		        request.addHeader("content-type", "application/xml");
		        request.setEntity(new StringEntity(requestData.toString(), "UTF-8"));
		        HttpResponse response = httpClient.execute(request);
		        HttpEntity entity = response.getEntity();
		        String responseString = EntityUtils.toString(entity, "UTF-8");
		        
				if (responseString.indexOf("Exception") > -1)
				{
					ResponseHelper.writeResponse(params, "Exception");
				}
				else if (responseString.indexOf("<wfs:totalUpdated>1</wfs:totalUpdated>") > -1)
				{
					ResponseHelper.writeResponse(params, "");
				}
            }
		}
		catch (JSONException ex) {
			log.error(ex, "JSON processing error");
			throw new ActionException("JSON processing error", ex);
		}
		catch (ClientProtocolException ex)
		{
			log.error(ex, "Geoserver connection error");
			throw new ActionException("Geoserver connection error", ex);
		}		
		catch (IOException ex)
		{
			log.error(ex, "IO error");
			throw new ActionException("IO error", ex);
		}
	}
	
	private void FillGeometries(StringBuilder requestData, JSONObject geometries) throws JSONException
	{
		try {
			String geometryType = geometries.getString("type");
			if (geometryType.equals("multipoint"))
			{
				FillMultiPointGeometries(requestData, geometries);
			}
			else if (geometryType.equals("multilinestring"))
			{
				FillLineStringGeometries(requestData, geometries);
			}
			else if (geometryType.equals("multipolygon"))
			{
				FillPolygonGeometries(requestData, geometries);
			}
		}
		catch (JSONException ex)
		{
			throw ex;
		}
	}
	
	private void FillMultiPointGeometries (StringBuilder requestData, JSONObject geometries) throws JSONException
	{
		try {
			String tmp = "";	
			JSONArray data = geometries.getJSONArray("data");
			requestData.append("<wfs:Property><wfs:Name>" + geometryProperty + "</wfs:Name><wfs:Value><gml:MultiPoint xmlns:gml='http://www.opengis.net/gml' srsName='http://www.opengis.net/gml/srs/epsg.xml#3067'>");
			for (int i = 0; i < data.length(); i++)
			{
				requestData.append("<gml:pointMember><gml:Point><gml:coordinates decimal=\".\" cs=\",\" ts=\" \">" + data.getJSONObject(i).getString("x") + "," + data.getJSONObject(i).getString("y") + "</gml:coordinates></gml:Point></gml:pointMember>");
			}
			requestData.append("</gml:MultiPoint></wfs:Value></wfs:Property>");
		}
		catch (JSONException ex)
		{
			throw ex;
		}
	}
	
	private void FillLineStringGeometries (StringBuilder requestData, JSONObject geometries) throws JSONException
	{
		try {
			String tmp = "";	
			JSONArray data = geometries.getJSONArray("data");
			requestData.append("<wfs:Property><wfs:Name>" + geometryProperty + "</wfs:Name><wfs:Value><gml:MultiLineString xmlns:gml='http://www.opengis.net/gml' srsName='http://www.opengis.net/gml/srs/epsg.xml#3067'>");
			for (int i = 0; i < data.length(); i++)
			{
				requestData.append("<gml:lineStringMember><gml:LineString><gml:coordinates decimal=\".\" cs=\",\" ts=\" \">");
				JSONArray arr = data.getJSONArray(i);
				for (int j = 0; j < arr.length(); j++)
				{
					requestData.append(arr.getJSONObject(j).getString("x") + "," + arr.getJSONObject(j).getString("y"));
					if (j < (arr.length()-1))
					{
						requestData.append(" ");
					}
				}
				requestData.append("</gml:coordinates></gml:LineString></gml:lineStringMember>");
			}
			requestData.append("</gml:MultiLineString></wfs:Value></wfs:Property>");
		}
		catch (JSONException ex)
		{
			throw ex;
		}
	}
	
	private void FillPolygonGeometries (StringBuilder requestData, JSONObject geometries) throws JSONException
	{
		try {
			String tmp = "";	
			JSONArray data = geometries.getJSONArray("data");
			requestData.append("<wfs:Property><wfs:Name>" + geometryProperty + "</wfs:Name><wfs:Value><gml:MultiPolygon xmlns:gml='http://www.opengis.net/gml' srsName='http://www.opengis.net/gml/srs/epsg.xml#3067'>");
			for (int i = 0; i < data.length(); i++)
			{
				requestData.append("<gml:polygonMember><gml:Polygon>");
				JSONArray arr = data.getJSONArray(i);
				for (int j = 0; j < arr.length(); j++)
				{
					if (j > 0) {
						requestData.append("<gml:interior><gml:LinearRing><gml:posList>");
					} else {
						requestData.append("<gml:exterior><gml:LinearRing><gml:posList>");
					}
					
					JSONArray arr2 = arr.getJSONArray(j);
					for (int k = 0; k < arr2.length(); k++)
					{
						requestData.append(arr2.getJSONObject(k).getString("x") + " " + arr2.getJSONObject(k).getString("y"));
						if (k < (arr2.length()-1))
						{
							requestData.append(" ");
						}
					}
					
					if (j > 0) {
						requestData.append("</gml:posList></gml:LinearRing></gml:interior>");
					} else {
						requestData.append("</gml:posList></gml:LinearRing></gml:exterior>");
					}
				}
				requestData.append("</gml:Polygon></gml:polygonMember>");
			}
			requestData.append("</gml:MultiPolygon></wfs:Value></wfs:Property>");
		}
		catch (JSONException ex)
		{
			throw ex;
		}
	}

	private void ClearLayerTiles(int layerId)
	{
		Set<String> keys = JedisManager.keys(KEY + Integer.toString(layerId));
		
		for(String key : keys)
		{
			JedisManager.del(key);
		}
	}
}

