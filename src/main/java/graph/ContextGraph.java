
package graph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "Edges",
    "NodeLabels"
})
public class ContextGraph {

    @JsonProperty("Edges")
    public Edges edges;
    @JsonProperty("NodeLabels")
    public Map<String, String> nodeLabels;

    /**
     * No args constructor for use in serialization
     *
     */
    public ContextGraph() {
    }

    /**
     *
     * @param edges
     * @param nodeLabels
     */
    public ContextGraph(Edges edges, Map<String, String> nodeLabels) {
        super();
        this.edges = edges;
        this.nodeLabels = nodeLabels;
    }

}
