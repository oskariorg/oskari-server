package fi.nls.oskari.ontology.service;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.ontology.domain.Keyword;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Oskari
public class KeywordServiceMybatisImpl extends KeywordService {

    private static final Logger log = LogFactory.getLogger(KeywordServiceMybatisImpl.class);

    private SqlSessionFactory factory = null;

    public KeywordServiceMybatisImpl() {
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        DataSource dataSource = helper.getDataSource();
        if (dataSource == null) {
            dataSource = helper.createDataSource();
        }
        if (dataSource == null) {
            log.error("Couldn't get datasource for keywordservice");
        }
        factory = initializeMyBatis(dataSource);
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.getTypeAliasRegistry().registerAlias(Keyword.class);
        configuration.setLazyLoadingEnabled(true);
        configuration.addMapper(KeywordMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }

    public List<Keyword> findKeywordsMatching(final String name) {
        if(name == null) {
            return Collections.emptyList();
        }

        final SqlSession session = factory.openSession();
        List<Keyword> keywordList = null;
        try {
            log.debug("Finding keywords matching: ", name);
            final KeywordMapper mapper = session.getMapper(KeywordMapper.class);
            keywordList =  mapper.findKeywordsMatching(name.toLowerCase());
            if(keywordList == null) {
                keywordList = Collections.emptyList();
            }
            log.debug("Found keywords: ", keywordList);
            return keywordList;

        } catch (Exception e) {
            log.warn(e, "Exception when trying to load keyword with name: ", name);
        } finally {
            session.close();
        }
        return Collections.emptyList();
    }

    public List<Keyword> findKeywordsMatching(final String name, final String lang) {
        if(name == null) {
            return Collections.emptyList();
        }

        final SqlSession session = factory.openSession();
        List<Keyword> keywordList = null;
        try {
            log.debug("Finding keywords matching: ", name);
            final Keyword param = new Keyword();
            param.setValue(name.toLowerCase());
            param.setLang(lang);

            final KeywordMapper mapper = session.getMapper(KeywordMapper.class);
            keywordList =  mapper.findMatchingKeyword(param);
            if(keywordList == null) {
                keywordList = Collections.emptyList();
            }
            log.debug("Found keywords: ", keywordList);
        } catch (Exception e) {
            log.warn(e, "Exception when trying to load keyword with name: ", name);
        } finally {
            session.close();
        }
        return keywordList;
    }

    public Keyword findExactKeyword(final String name, final String language) {
        if(name == null) {
            return null;
        }

        final SqlSession session = factory.openSession();
        Keyword keyword = null;
        try {
            log.debug("Finding keyword template by name: ", name);
            final Keyword param = new Keyword();
            param.setValue(name.toLowerCase());
            param.setLang(language);

            final KeywordMapper mapper = session.getMapper(KeywordMapper.class);
            final List<Keyword> keywordList =  mapper.findExactKeyword(param);

            if(keywordList == null) {
                return null;
            }

            log.debug("Found keywords: ", keywordList);
            if(keywordList.size() > 0) {
                keyword = keywordList.get(0);
            }

            return keyword;

        } catch (Exception e) {
            log.warn(e, "Exception when trying to load keyword with name: ", name);
        } finally {
            session.close();
        }
        return keyword;
    }

    public List<Keyword> findSynonyms(final Long id, final String language) {
        if (id == null) {
            return null;
        }

        final SqlSession session = factory.openSession();
        List<Keyword> synonymList = null;
        try {
            log.debug("Finding synonyms matching: ", id);
            final Keyword param = new Keyword();
            param.setId(id);
            param.setLang(language);

            final KeywordMapper mapper = session.getMapper(KeywordMapper.class);
            synonymList =  mapper.findSynonyms(param);
            if(synonymList == null) {
                synonymList = Collections.emptyList();
            }
            log.debug("Found " + synonymList.size() + " synonyms");
            return synonymList;

        } catch (Exception e) {
            log.warn(e, "Exception when trying to load synonyms with id: ", id);
        } finally {
            session.close();
        }
        return synonymList;
    }

    public List<Keyword> findParents(final Long id, final String language) {
        if (id == null) {
            return null;
        }

        final SqlSession session = factory.openSession();
        List<Keyword> parentList = null;
        try {
            log.debug("Finding parents matching: ", id);
            final Keyword param = new Keyword();
            param.setId(id);
            param.setLang(language);

            final KeywordMapper mapper = session.getMapper(KeywordMapper.class);
            parentList =  mapper.findParents(param);
            if(parentList == null) {
                parentList = Collections.emptyList();
            }
            log.debug("Found " + parentList.size() + " parents");

        } catch (Exception e) {
            log.warn(e, "Exception when trying to load synonyms with id: ", id);
        } finally {
            session.close();
        }
        return parentList;
    }

    public List<Keyword> findSiblings(final Long id, final String language) {
        if (id == null) {
            return null;
        }

        final SqlSession session = factory.openSession();
        List<Keyword> siblingList = null;
        try {
            log.debug("Finding siblings matching: ", id);
            final Keyword param = new Keyword();
            param.setId(id);
            param.setLang(language);

            final KeywordMapper mapper = session.getMapper(KeywordMapper.class);
            siblingList =  mapper.findParents(param);
            if(siblingList == null) {
                siblingList = Collections.emptyList();
            }
            log.debug("Found " + siblingList.size() + " siblings");

        } catch (Exception e) {
            log.warn(e, "Exception when trying to load siblings with id: ", id);
        } finally {
            session.close();
        }
        return siblingList;
    }

    public List<Keyword> findKeywordsForLayer(final Long layerId) {
        final SqlSession session = factory.openSession();
        List<Keyword> keywordList = null;
        try {
            log.debug("Finding keywords for layer: ", layerId);
            final KeywordMapper mapper = session.getMapper(KeywordMapper.class);
            keywordList =  mapper.findKeywordForLayer(layerId);
            if(keywordList == null) {
                keywordList = Collections.emptyList();
            }
            log.debug("Found keywords:", keywordList);

        } catch (Exception e) {
            log.warn(e, "Exception when trying to load keywords for layer: ", layerId);
        } finally {
            session.close();
        }
        return keywordList;
    }

    public List<Long> findKeywordIdsLinkedLayer(final Long layerId) {
        final SqlSession session = factory.openSession();
        List<Long> keywordIdList = null;
        try {
            log.debug("Finding keywords ids linked to layer: ", layerId);
            final KeywordMapper mapper = session.getMapper(KeywordMapper.class);
            keywordIdList =  mapper.findKeywordIdsLinkedToLayer(layerId);
            if(keywordIdList == null) {
                keywordIdList = Collections.emptyList();
            }
            log.debug("Found keyword ids:", keywordIdList);
        } catch (Exception e) {
            log.warn(e, "Exception when trying to load keyword ids for layer: ", layerId);
        } finally {
            session.close();
        }
        return keywordIdList;
    }

    public long addKeyword(final Keyword keyword) {
        // check if keyword is already inserted with matching language and return the id without inserting!
        final Keyword dbKey = findExactKeyword(keyword.getValue(), keyword.getLang());
        if(dbKey != null) {
            log.warn("Keyword already saved:", keyword, "->", dbKey);
            keyword.setId(dbKey.getId());
            return dbKey.getId();
        }
        final SqlSession session = factory.openSession();
        try {
            log.debug("Adding keyword: ", keyword);
            final KeywordMapper mapper = session.getMapper(KeywordMapper.class);
            mapper.addKeyword(keyword);
            session.commit();
        } catch (Exception e) {
            log.warn(e, "Exception when trying to add keyword: ", keyword);
        } finally {
            session.close();
        }
        log.warn("Got keyword id:", keyword.getId());
        return keyword.getId();
    }

    public void linkKeywordToLayer(final Long keywordId, final Long layerId) {
        // check if keyword is already inserted with matching language and return the id without inserting!
        List<Long> keyIdList = findKeywordIdsLinkedLayer(layerId);
        for (Long keyId : keyIdList) {
            if (keyId.equals(keywordId)) {
                // already linked
                return;
            }
        }
        // not linked yet - work it
        final SqlSession session = factory.openSession();
        try {
            log.debug("Linking keyword to layer: ", layerId);
            final KeywordMapper mapper = session.getMapper(KeywordMapper.class);
            Map<String, Long> params = new HashMap<>();
            params.put("keyid", keywordId);
            params.put("layerid", layerId);
            mapper.linkKeywordToLayer(params);
            session.commit();
            log.warn("Linked keyword to layer");
        } catch (Exception e) {
            log.warn(e, "Exception when trying to link keyword to layer: ", layerId);
        } finally {
            session.close();
        }
    }
}
