package fi.nls.oskari.routing.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlanConnection {
    @JsonProperty("searchDateTime")
    private String searchDateTime;

    @JsonProperty("edges")
    private List<Edge> edges;

    @JsonProperty("routingErrors")
    private List<RoutingError> routingErrors;


    public String getSearchDateTime() {
        return searchDateTime;
    }

    public void setSearchDateTime(String searchDateTime) {
        this.searchDateTime = searchDateTime;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }

    public List<RoutingError> getRoutingErrors() {
        return routingErrors;
    }

    public void setRoutingErrors(List<RoutingError> routingErrors) {
        this.routingErrors = routingErrors;
    }
}
