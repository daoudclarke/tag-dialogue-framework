package uk.ac.susx.tag.dialoguer.knowledge.location;

import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Andrew D. Robertson on 06/08/2014.
 */
public class ResultsElement {

    public final String type;
    public final long id;
    public double lat;
    public double lon;
    public double radius;
    public final Map<String, String> tags;
    public final List<Long> nodes;

    public ResultsElement(String type, long id, double lat, double lon, double radius, Map<String, String> tags, List<Long> nodes) {
        this.type = type;
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.radius = radius;
        this.tags = tags;
        this.nodes = nodes;
    }

    public boolean hasTag(String tag){
        return tags != null && tags.containsKey(tag);
    }

    /**
     * return true if this element has ANY of the tags in *tags*
     */
    public boolean hasTag(Set<String> tags){
        return Sets.intersection(this.tags.keySet(), tags).size() > 0;
    }

    public boolean hasTagValue(String tag, String value){
        return tags != null && tags.containsKey(tag) && tags.get(tag).equals(value);
    }

    public String getTagValue(String tag){
        return tags.containsKey(tag)? tags.get(tag) : null;
    }
}