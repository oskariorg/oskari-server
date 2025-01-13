package org.oskari.control.layer.status;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.JSONException;
import org.json.JSONObject;

public class LayerStatus {

    private final String id;
    private long errors = 0;
    private long success = 0;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    LayerStatus(@JsonProperty("id") String id) {
        this.id = id;
    }

    LayerStatus(String id, JSONObject data) {
        this(id);
        this.errors = data.optLong("errors");
        this.success = data.optLong("success");
    }

    public void addToSuccess(long amount) {
        success += amount;
    }

    public void addToErrors(long amount) {
        errors += amount;
    }

    public String getId() {
        return id;
    }

    public long getErrors() {
        return errors;
    }

    public long getSuccess() {
        return success;
    }
    
    @JsonIgnore
    public long getRequestCount() {
        return success + errors;
    }

    @JsonIgnore
    public JSONObject asJSON() throws JSONException {
        JSONObject response = new JSONObject();
        response.put("id", getId());
        response.put("success", getSuccess());
        response.put("errors", getErrors());
        return response;
    }
}
