package fi.nls.oskari.control.feature;

import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.data.domain.OskariLayerResource;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.permission.domain.Resource;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.wfs.WFSLayerConfigurationService;
import fi.nls.oskari.wfs.WFSLayerConfigurationServiceIbatisImpl;
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

import java.io.IOException;
import java.util.Set;


public abstract class AbstractFeatureHandler extends ActionHandler {

    public final static String CACHKE_KEY_PREFIX = "WFSImage_";

    private OskariLayerService layerService;
    private PermissionsService permissionsService;
    private WFSLayerConfigurationService layerConfigurationService;

    @Override
    public void init() {
        super.init();
        layerService = new OskariLayerServiceIbatisImpl();
        permissionsService = new PermissionsServiceIbatisImpl();
        layerConfigurationService = new WFSLayerConfigurationServiceIbatisImpl();
    }

    protected OskariLayer getLayer(String id) throws ActionParamsException {
        return layerService.find(getLayerId(id));
    }

    protected boolean canEdit(OskariLayer layer, User user) {
        final Resource resource = permissionsService.findResource(new OskariLayerResource(layer));
        return resource.hasPermission(user, Permissions.PERMISSION_TYPE_EDIT_LAYER_CONTENT);
    }

    protected int getLayerId(String layerId) throws ActionParamsException {
        int id = ConversionHelper.getInt(layerId, -1);
        if (id == -1) {
            throw new ActionParamsException("Missing layer id");
        }
        return id;
    }

    protected String postPayload(OskariLayer layer, String payload) throws ActionException, IOException {

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        Credentials credentials = new UsernamePasswordCredentials(layer.getUsername(), layer.getPassword());
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY, credentials);

        httpClientBuilder.setDefaultCredentialsProvider(credsProvider);
        HttpClient httpClient = httpClientBuilder.build();
        HttpPost request = new HttpPost(layer.getUrl());
        request.addHeader("content-type", "application/xml");
        request.setEntity(new StringEntity(payload, "UTF-8"));
        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity, "UTF-8");
    }

    protected void flushLayerTilesCache(int layerId) {
        Set<String> keys = JedisManager.keys(CACHKE_KEY_PREFIX + Integer.toString(layerId));
        for (String key : keys) {
            JedisManager.delAll(key);
        }
    }
}
