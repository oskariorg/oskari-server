package fi.nls.oskari.ontology.domain;

import java.util.*;

/**
 * @author SMAKINEN
 */
public class Keyword {

    private Long id;
    private String value;
    private String uri;
    private String lang;
    private boolean editable = false;

    private List<Long> layerIds = new ArrayList<Long>();

    private Map<RelationType, Set<Keyword>> relations = new HashMap<RelationType, Set<Keyword>>();

    public Set<Keyword> getRelated(final RelationType rel) {
        Set<Keyword> set = relations.get(rel);
        if(set == null) {
            set = new HashSet<Keyword>();
            relations.put(rel, set);
        }
        return set;
    }

    public void addRelation(final RelationType rel, final Keyword word) {
        final Set<Keyword> set = getRelated(rel);
        set.add(word);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public List<Long> getLayerIds() {
        return layerIds;
    }

    public void setLayerIds(List<Long> layerIds) {
        this.layerIds = layerIds;
    }
}
