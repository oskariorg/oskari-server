package fi.nls.oskari.control.admin;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.Set;

@OskariActionRoute("Cache")
public class CacheHandler extends RestActionHandler {

    private Logger log = LogFactory.getLogger(CacheHandler.class);

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        final JSONObject response = new JSONObject();
        final JSONArray list = new JSONArray();
        Set<String> cacheNames = CacheManager.getCacheNames();
        for (String name : cacheNames) {
            Cache cache = CacheManager.getCache(name);
            final JSONObject json = new JSONObject();
            JSONHelper.putValue(json, "name", cache.getName());
            JSONHelper.putValue(json, "size", cache.getSize());
            JSONHelper.putValue(json, "limit", cache.getLimit());
            JSONHelper.putValue(json, "expiration", (cache.getExpiration() / 1000));
            JSONHelper.putValue(json, "lastFlush", (cache.getLastFlush() / 1000));
            list.put(json);
        }
        JSONHelper.putValue(response, "caches", list);
        JSONHelper.putValue(response, "timestamp", new Date());
        ResponseHelper.writeResponse(params, response);
    }


    @Override
    public void preProcess(ActionParameters params) throws ActionException {
        if (!params.getUser().isAdmin()) {
            throw new ActionDeniedException("Admin only");
        }
    }

}