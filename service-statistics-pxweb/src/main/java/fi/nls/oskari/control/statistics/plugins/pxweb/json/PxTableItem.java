package fi.nls.oskari.control.statistics.plugins.pxweb.json;

/**
 * Created by JacksonGenerator on 28.3.2018.
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import fi.nls.oskari.control.statistics.data.IdNamePair;

import java.util.Collections;
import java.util.List;


public class PxTableItem {
    @JsonProperty("variables")
    private List<VariablesItem> variables;
    @JsonProperty("title")
    private String title;

    private String id;
    private String path;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public List<VariablesItem> getSelectors() {
        if(variables == null) {
            return Collections.emptyList();
        }
        return variables;
    }
    public VariablesItem getVariable(String variableCode) {
        return getSelectors()
                .stream()
                .filter(x -> x.getCode().equals(variableCode))
                .findFirst()
                .orElse(null);
    }
}