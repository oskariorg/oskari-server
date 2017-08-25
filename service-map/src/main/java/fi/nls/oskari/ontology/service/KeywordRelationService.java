package fi.nls.oskari.ontology.service;

import fi.nls.oskari.ontology.domain.Relation;

import java.sql.SQLException;
import java.util.List;

/**
 * User: TMIKKOLAINEN
 * Date: 23.8.2013
 * Time: 13:37
 * Service for keyword ontology relations
 */
public interface KeywordRelationService {
    /**
     * Adds a relationship between the two keywords: <keyword1> has <keyword2> as a <relationType>.
     * Also adds the inverse relationship.
     * @param relation
     */
    void addRelation(final Relation relation);

    /**
     * Returns the exact match for the given relation
     * @param relation
     * @return Relation
     */
    Relation getRelation(final Relation relation);

    /**
     * Returns all relations for the given keyword
     * @param keyId
     * @return List of relations
     */
    List<Relation> getRelationsForKeyword(final Long keyId);

    /**
     * Returns relations of given type for the given keyword.
     * Note that we only return non-symmetrical relations in the proper direction,
     * i.e we only return AKs for the given keyword, not the keywords that have it as an AK.
     * @param relation
     * @return List of Relations
     */
    List<Relation> getRelationsByTypeForKeyword(final Relation relation);

    /**
     * Deletes all relations so they can be rebuilt from scratch
     */
    void deleteAllRelations() throws SQLException;

}
