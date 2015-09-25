package fi.nls.oskari.control.feature;

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.data.domain.OskariLayerResource;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.permission.domain.Resource;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("SaveFeature")
public class SaveFeatureHandler extends ActionHandler {
	private OskariLayerService layerService;
	private PermissionsService permissionsService;
	
	@Override
	public void handleAction(ActionParameters params) throws ActionException {
		layerService = new OskariLayerServiceIbatisImpl();
		permissionsService = new PermissionsServiceIbatisImpl();
		String featureData = params.getHttpParam("featureData");
		String url = PropertyUtil.get("geoserver.wfs.url");
		String user = (PropertyUtil.get("geoserver.wfs.user").equals("--geoserver.wfs.user--") ? "admin": PropertyUtil.get("geoserver.wfs.user"));
		String pass = (PropertyUtil.get("geoserver.wfs.pass").equals("--geoserver.wfs.pass--") ? "geoserver": PropertyUtil.get("geoserver.wfs.pass"));

		try {
			JSONObject jsonObject = new JSONObject(featureData);
			OskariLayer lay = layerService.find(jsonObject.getString("layerId"));
			final Resource resource = permissionsService.findResource(new OskariLayerResource(lay));
            final boolean hasPermssion = resource.hasPermission(params.getUser(), Permissions.PERMISSION_TYPE_EDIT_LAYER);
            if(hasPermssion) {
				StringBuilder requestData = new StringBuilder("<wfs:Transaction service='WFS' version='1.1.0' xmlns:topp='http://www.openplans.org/topp' xmlns:ogc='http://www.opengis.net/ogc' xmlns:wfs='http://www.opengis.net/wfs'><wfs:Update typeName='"+ jsonObject.getString("layerName") +"'>");
				JSONArray jsonArray = jsonObject.getJSONArray("featureFields");
				for (int i = 0; i < jsonArray.length(); i++) {
					 	requestData.append("<wfs:Property><wfs:Name>" + jsonArray.getJSONObject(i).getString("key") + "</wfs:Name><wfs:Value>" + jsonArray.getJSONObject(i).getString("value") + "</wfs:Value></wfs:Property>");
				}
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
		catch (Exception ex) {
			throw new ActionException("Could not update feature");
		}
	}
}

