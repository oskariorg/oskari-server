package fi.nls.oskari.ontology.service;

import fi.nls.oskari.ontology.domain.Keyword;

import java.util.List;
import java.util.Map;

public interface KeywordMapper {

    List<Keyword> findKeywordsMatching(final String name);
    List<Keyword> findMatchingKeyword(final Keyword keyword);
    List<Keyword> findExactKeyword(final Keyword keyword);
    List<Keyword> findSynonyms(final Keyword keyword);
    List<Keyword> findParents(final Keyword keyword);
    List<Keyword> findSiblings(final Long id, final String language);
    List<Keyword> findKeywordForLayer(final Long layerId);
    List<Long> findKeywordIdsLinkedToLayer(final Long layerId);
    long addKeyword(final Keyword keyword);
    void linkKeywordToLayer(Map<String, Long> params);
}