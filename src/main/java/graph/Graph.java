
package graph;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "filename",
    "ContextGraph",
    "skill"
})
public class Graph {

    @JsonProperty("filename")
    public String filename;
    @JsonProperty("ContextGraph")
    public ContextGraph contextGraph;
    @JsonProperty("skill")
    public Integer skill;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Graph() {
    }

    /**
     * 
     * @param contextGraph
     * @param filename
     * @param skill
     */
    public Graph(String filename, ContextGraph contextGraph, Integer skill) {
        super();
        this.filename = filename;
        this.contextGraph = contextGraph;
        this.skill = skill;
    }

}
