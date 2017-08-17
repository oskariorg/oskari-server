package fi.nls.oskari.ontology.service;

import fi.nls.oskari.ontology.domain.Keyword;
import fi.nls.oskari.service.OskariComponent;

import java.util.List;

public abstract class KeywordService extends OskariComponent {

    public abstract List<Keyword> findKeywordsMatching(final String name);
    public abstract List<Keyword> findKeywordsMatching(final String name, final String lang);
    public abstract Keyword findExactKeyword(final String name, final String language);
    public abstract List<Keyword> findSynonyms(final Long id, final String language);
    public abstract List<Keyword> findParents(final Long id, final String language);
    public abstract List<Keyword> findSiblings(final Long id, final String language);
    public abstract long addKeyword(final Keyword keyword);
    public abstract void linkKeywordToLayer(final Long keywordId, final Long layerId);
}
