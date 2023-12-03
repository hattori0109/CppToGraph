
package graph;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "Child",
    "NextToken",
    "LastLexicalUse"
})
public class Edges {

    @JsonProperty("Child")
    public List<List<Integer>> child = null;
    @JsonProperty("NextToken")
    public List<List<Integer>> nextToken = null;
    @JsonProperty("LastLexicalUse")
    public List<List<Integer>> lastLexicalUse = null;

    /**
     * No args constructor for use in serialization
     *
     */
    public Edges() {
    }

    /**
     *
     * @param nextToken
     * @param child
     */
    public Edges(List<List<Integer>> child, List<List<Integer>> nextToken, List<List<Integer>> lastLexicalUse) {
        super();
        this.child = child;
        this.nextToken = nextToken;
        this.lastLexicalUse = lastLexicalUse;
    }

}
