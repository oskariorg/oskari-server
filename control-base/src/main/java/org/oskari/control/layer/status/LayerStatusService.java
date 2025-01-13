package org.oskari.control.layer.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Oskari
public class LayerStatusService extends OskariComponent {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String REDIS_KEY = "LayerStatus";
    private Logger log = LogFactory.getLogger("STATUS");

    public List<LayerStatus> getStatuses() {
        return listFromRedis();
    }

    public List<JSONObject> getMostErrors(int limit) {
        List<JSONObject> mostErrors = getStatuses().stream()
                .sorted(Comparator.comparingLong(LayerStatus::getErrors).reversed())
                .limit(limit)
                .map(layer -> {
                    JSONObject o = new JSONObject();
                    JSONHelper.putValue(o, "id", layer.getId());
                    JSONHelper.putValue(o, "errors", layer.getErrors());
                    JSONHelper.putValue(o, "success", layer.getSuccess());
                    return o;
                })
                .collect(Collectors.toList());
        return mostErrors;
    }

    public List<JSONObject> getMostUsed(int limit) {
        List<JSONObject> mostSuccess = getStatuses().stream()
                .sorted(Comparator.comparingLong(LayerStatus::getRequestCount).reversed())
                .limit(limit)
                .map(layer -> {
                    JSONObject o = new JSONObject();
                    JSONHelper.putValue(o, "id", layer.getId());
                    JSONHelper.putValue(o, "errors", layer.getErrors());
                    JSONHelper.putValue(o, "success", layer.getSuccess());
                    return o;
                })
                .collect(Collectors.toList());
        return mostSuccess;
    }

    // {801: {errors: 0, success: 73, stack: [], previous: "success"}}
    public void saveStatus(JSONObject payload) {
        payload.keys().forEachRemaining(layerId -> {
            String id = (String) layerId;
            JSONObject layerData = payload.optJSONObject(id);
            // we don't really care about the previous key as it's used by
            //  frontend to detect state change between failure <> success
            layerData.remove("previous");
            long errorCount = layerData.optLong("errors", 0);
            updateToRedis(
                    id,
                    layerData.optLong("success", 0),
                    errorCount
            );
            if (errorCount != 0) {
                saveStack(id, layerData);
            }
            // write log to get stacks for error debugging
            log.info(layerId, "-", layerData.toString());
        });
    }

    private List<LayerStatus> listFromRedis() {
        Set<String> keys = JedisManager.hkeys(REDIS_KEY);
        return keys.stream()
                .map(layerId -> getEntry(layerId))
                .collect(Collectors.toList());
    }

    public JSONObject getDetails(String id) {
        try {
            LayerStatus status = getEntry(id);
            JSONObject response = status.asJSON();
            response.put("details", new JSONArray(getRawDataFromRedis(id)));
            return response;
        } catch (Exception ignored) {}
        return null;
    }

    public void removeLayerStatus(String id) {
        JedisManager.hdel(REDIS_KEY, id);
    }

    public void removeLayerRawData(String id, String dataId) {
        String redisKey = getRawDataKeyForRedis(id);
        JedisManager.hdel(redisKey, dataId);
    }

    private List<JSONObject> getRawDataFromRedis(String id) {
        String redisKey = getRawDataKeyForRedis(id);
        Set<String> keys = JedisManager.hkeys(redisKey);

        return keys.stream()
                .map(dataId -> getRawDataFromRedis(redisKey, dataId))
                .filter(data -> data != null)
                .collect(Collectors.toList());
    }

    private JSONObject getRawDataFromRedis(String redisKey, String rawDataId) {
        String data = JedisManager.hget(redisKey, rawDataId);
        try {
            JSONObject value = new JSONObject(data);
            // raw data id is System.currentTimeMillis() as string
            value.put("time", Long.parseLong(rawDataId));
            return value;
        } catch (Exception ignored) {
            log.warn("Unable to deserialize rawdata for key:", redisKey, "dataId:", rawDataId);
        }
        return null;
    }

    private void updateToRedis(String id, long success, long errors) {
        // TODO: should use https://redis.io/commands/hincrby instead or save to postgres?
        LayerStatus status = getEntry(id);
        status.addToErrors(errors);
        status.addToSuccess(success);
        JedisManager.hset(REDIS_KEY, id, writeAsJSON(status));
        // TODO: bake id into key and use date string as field (current id) to get time dimension?
        // Set<String> keys = JedisManager.hkeys(REDIS_KEY)
    }

    private void saveStack(String id, JSONObject dataFromUser) {
        JSONArray stack = dataFromUser.optJSONArray("stack");
        if (stack == null || stack.length() == 0) {
            return;
        }
        JedisManager.hset(getRawDataKeyForRedis(id), "" + System.currentTimeMillis(), dataFromUser.toString());
        // we could use list but JedisManager only has getters for list that modify it
        // If we don't move this to postgres then we might want to add both the increment method and list getters to JedisManager
        //  Note! increment added in https://github.com/oskariorg/oskari-server/pull/729
        //  List handling seems a bit unwieldy via Redis if we would want to remove an item from the list with other than l/rpop()
        //    so I would rather do it with postgres if we need that
        // JedisManager.pushToList(getRawDataKeyForRedis(id), value.toString());
    }

    private String getRawDataKeyForRedis(String id) {
        return REDIS_KEY + "_" + id + "_raw";
    }

    private LayerStatus getEntry(String id) {
        String data = JedisManager.hget(REDIS_KEY, id);
        if (data == null) {
            return new LayerStatus(id);
        }
        return readFromJSON(data);
    }

    private LayerStatus readFromJSON(String status) {
        try {
            return MAPPER.readValue(status, LayerStatus.class);
        } catch (JsonProcessingException e) {
            throw new ServiceRuntimeException("Unable to deserialize status", e);
        }
    }

    private String writeAsJSON(LayerStatus status) {
        try {
            return MAPPER.writeValueAsString(status);
        } catch (JsonProcessingException e) {
            throw new ServiceRuntimeException("Unable to serialize status", e);
        }
    }
}