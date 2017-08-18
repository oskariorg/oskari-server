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
public interface KeywordRelationMapper {
    void insert(final Relation relation);
    Relation getRelation(final Relation relation);
    List<Relation> getRelationsForKeyword(final Long keyId);
    List<Relation> getRelationsByTypeForKeyword(final Relation relation);
    void deleteAllRelations() throws SQLException;
}
