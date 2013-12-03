package fi.nls.oskari.ontology.service;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.ontology.domain.Keyword;
import fi.nls.oskari.service.db.BaseIbatisService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeywordServiceIbatisImpl extends BaseIbatisService<Keyword> implements KeywordService {

    private static final Logger log = LogFactory.getLogger(KeywordServiceIbatisImpl.class);

    @Override
    protected String getNameSpace() {
        return "Keyword";
    }

    public List<Keyword> findKeywordsMatching(final String name) {
        if(name == null) {
            return Collections.emptyList();
        }
        log.debug("Finding keywords matching:", name);
        final List<Keyword> keyword = queryForList(getNameSpace() + ".find-by-name", name.toLowerCase());
        log.debug("Found keywords:", keyword);
        return keyword;
    }

    public List<Keyword> findKeywordsMatching(final String name, final String lang) {
        if(name == null) {
            return Collections.emptyList();
        }
        log.debug("Finding keywords matching:", name);
        final Keyword param = new Keyword();
        param.setValue(name.toLowerCase());
        param.setLang(lang);
        final List<Keyword> keyword = queryForList(getNameSpace() + ".find-by-name-and-lang", param);
        log.debug("Found keywords:", keyword);
        return keyword;
    }

    public Keyword findExactKeyword(final String name, final String language) {
        if(name == null) {
            return null;
        }
        log.debug("Finding keyword template by name:", name);
        final Keyword param = new Keyword();
        param.setValue(name.toLowerCase());
        param.setLang(language);
        final Keyword keyword = queryForObject(getNameSpace() + ".find-exact-by-name-and-lang", param);
        log.debug("Found keyword:", keyword);
        return keyword;
    }

    public List<Keyword> findSynonyms(final Long id, final String language) {
        if (id == null) {
            return null;
        }
        final Keyword param = new Keyword();
        param.setId(id);
        param.setLang(language);
        final List<Keyword> synonyms = queryForList(getNameSpace() + ".find-synonyms-by-id-and-lang", param);
        log.debug("Found " + synonyms.size() + " synonyms");
        return synonyms;
    }

    public List<Keyword> findParents(final Long id, final String language) {
        if (id == null) {
            return null;
        }
        final Keyword param = new Keyword();
        param.setId(id);
        param.setLang(language);
        final List<Keyword> synonyms = queryForList(getNameSpace() + ".find-parents-by-id-and-lang", param);
        log.debug("Found " + synonyms.size() + " synonyms");
        return synonyms;
    }

    public List<Keyword> findSiblings(final Long id, final String language) {
        if (id == null) {
            return null;
        }
        final Keyword param = new Keyword();
        param.setId(id);
        param.setLang(language);
        final List<Keyword> synonyms = queryForList(getNameSpace() + ".find-siblings-by-id-and-lang", param);
        log.debug("Found " + synonyms.size() + " synonyms");
        return synonyms;
    }

    public List<Keyword> findKeywordsForLayer(final Long layerId) {
        log.debug("Finding keywords for layer:", layerId);
        final List<Keyword> keyword = queryForList(getNameSpace() + ".find-by-layerId", layerId);
        log.debug("Found keywords:", keyword);
        return keyword;
    }

    public List<Long> findKeywordIdsLinkedLayer(final Long layerId) {
        return queryForList(getNameSpace() + ".find-keyIds-linked-to-layer", layerId);
    }

    public long addKeyword(final Keyword keyword) {
        // check if keyword is already inserted with matching language and return the id without inserting!
        final Keyword dbKey = findExactKeyword(keyword.getValue(), keyword.getLang());
        if(dbKey != null) {
            log.debug("Keyword already saved:", keyword, "->", dbKey);
            keyword.setId(dbKey.getId());
            return dbKey.getId();
        }
        log.debug("Adding keyword:", keyword);
        final Long id = queryForObject(getNameSpace() + ".add-keyword", keyword);
        keyword.setId(id);
        log.debug("Got keyword id:", id);
        return id;
    }

    public void linkKeywordToLayer(final Long keywordId, final Long layerId) {
        // check if keyword is already inserted with matching language and return the id without inserting!
        List<Long> keyIdList = findKeywordIdsLinkedLayer(layerId);
        for(Long keyId : keyIdList) {
            if(keyId.equals(keywordId)) {
                // already linked
                return;
            }
        }
        // not linked yet - work it
        Map<String, Long> params = new HashMap<String, Long>();
        params.put("keyid", keywordId);
        params.put("layerid", layerId);
        queryForObject(getNameSpace() + ".link-keyword-to-layer", params);
    }


}
