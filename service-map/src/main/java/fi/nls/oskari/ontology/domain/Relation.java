package fi.nls.oskari.ontology.domain;

/**
 * Created with IntelliJ IDEA.
 * User: TMIKKOLAINEN
 * Date: 23.8.2013
 * Time: 13:40
 * To change this template use File | Settings | File Templates.
 */
public class Relation {
    private Long keyid1;
    private Long keyid2;
    private RelationType type;

    public Long getKeyid1() {
        return keyid1;
    }

    public void setKeyid1(Long keyid1) {
        this.keyid1 = keyid1;
    }

    public Long getKeyid2() {
        return keyid2;
    }

    public void setKeyid2(Long keyid2) {
        this.keyid2 = keyid2;
    }

    public RelationType getRelationType() {
        return type;
    }

    public void setRelationType(RelationType relationType) {
        // insert null if NONE so we won't store NONE to the DB by accident
        if (relationType == RelationType.NONE) {
            this.type = null;
        } else {
            this.type = relationType;
        }
    }
}
