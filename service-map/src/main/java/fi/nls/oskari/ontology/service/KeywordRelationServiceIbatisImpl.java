package fi.nls.oskari.ontology.service;

import fi.nls.oskari.ontology.domain.Relation;
import fi.nls.oskari.ontology.domain.RelationType;
import fi.nls.oskari.service.db.BaseIbatisService;

import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: TMIKKOLAINEN
 * Date: 23.8.2013
 * Time: 14:05
 * To change this template use File | Settings | File Templates.
 */
public class KeywordRelationServiceIbatisImpl  extends BaseIbatisService<Relation> implements KeywordRelationService {
    private static final String NAMESPACE = "KeywordRelation";

    @Override
    protected String getNameSpace() {
        return NAMESPACE;
    }

    public void addRelation(Relation relation) throws IllegalArgumentException {
        RelationType inverseRelationType = null;
        Relation inverseRelation = new Relation();
        inverseRelation.setKeyid1(relation.getKeyid2());
        inverseRelation.setKeyid2(relation.getKeyid1());

        switch(relation.getRelationType()) {
            case AK:
                // Store relationType in both directions, AK and YK
                inverseRelation.setRelationType(RelationType.YK);
                break;
            case YK:
                // store relationType in both directions YK and AK
                inverseRelation.setRelationType(RelationType.AK);
                break;
            case SYN:
            case LK:
            case VK:
                inverseRelation.setRelationType(relation.getRelationType());
                break;
            default:
                throw new IllegalArgumentException("Invalid relationType: " + relation.getRelationType());
        }

        // check that the relation doesn't already exist so we don't have to handle and exception for it...
        if (getRelation(relation) == null) {
            int maxId1 = insert(relation);
        }

        if (getRelation(inverseRelation) == null) {
            int maxId2 = insert(inverseRelation);
        }
    }

    public Relation getRelation(Relation relation) {
        return queryForObject(getNameSpace() + ".find-exact-by-ids-and-type", relation);
    }

    public List<Relation> getRelationsForKeyword(final Long keyId) {
        return queryForList(getNameSpace() + ".find-for-keyword", keyId);
    }

    public List<Relation> getRelationsByTypeForKeyword(Relation relation) {
        return queryForList(getNameSpace() + ".find-by-type-for-keyword", relation);
    }

    public void deleteAllRelations() throws SQLException {
        getSqlMapClient().delete(getNameSpace() + ".delete-all");
    }

}
