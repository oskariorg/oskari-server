package fi.nls.oskari.ontology.domain;

/**
 * Created with IntelliJ IDEA.
 * User: TMIKKOLAINEN
 * Date: 23.8.2013
 * Time: 13:41
 * RelationType flag to link keywords together.
 */
public enum RelationType {
    // FIXME check that these match what's used in ontology suggestions
    NONE(-1), // for exact matches and such
    AK(1),
    YK(2),
    SYN(3),
    LK(4),
    VK(5);

    final private int id;
    RelationType(final int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public RelationType find(final int id) {
        for(RelationType rel : RelationType.values()) {
            if(rel.getId() == id) {
                return rel;
            }
        }
        return RelationType.NONE;
    }
}
