package fi.nls.oskari.ontology.service;

import fi.nls.oskari.ontology.domain.Keyword;
import fi.nls.oskari.service.db.BaseService;

import java.util.List;

public interface KeywordService extends BaseService<Keyword> {

    public List<Keyword> findKeywordsMatching(final String name);
    public List<Keyword> findKeywordsMatching(final String name, String lang);
    public Keyword findExactKeyword(final String name, final String language);
    public List<Keyword> findSynonyms(final Long id, final String language);
    public List<Keyword> findParents(final Long id, final String language);
    public List<Keyword> findSiblings(final Long id, final String language);
    public long addKeyword(final Keyword keyword);
    public void linkKeywordToLayer(final Long keywordId, final Long layerId);
}
