package fi.nls.oskari.ontology.service;

import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.ontology.domain.Relation;
import fi.nls.oskari.ontology.domain.RelationType;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: TMIKKOLAINEN
 * Date: 23.8.2013
 * Time: 14:05
 * To change this template use File | Settings | File Templates.
 */
public class KeywordRelationServiceMybatisImpl implements KeywordRelationService {

    private static final Logger log = LogFactory.getLogger(KeywordRelationServiceMybatisImpl.class);

    private SqlSessionFactory factory = null;

    public KeywordRelationServiceMybatisImpl() {
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        final DataSource dataSource = helper.getDataSource(helper.getOskariDataSourceName("keywordrelation"));
        if(dataSource != null) {
            factory = initializeMyBatis(dataSource);
        }
        else {
            log.error("Couldn't get datasource for keywordrelationservice");
        }
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.setLazyLoadingEnabled(true);
        configuration.addMapper(KeywordMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
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
            //TODO int maxId1 = insert(relation);
        }

        if (getRelation(inverseRelation) == null) {
            //TODO int maxId2 = insert(inverseRelation);
        }
    }

    public Relation getRelation(Relation relation) {
        if(relation == null) {
            return null;
        }

        final SqlSession session = factory.openSession();
        Relation relationFound = null;
        try {
            log.debug("Finding keyword assiosiation: ", relation);
            final KeywordRelationMapper mapper = session.getMapper(KeywordRelationMapper.class);
            relationFound =  mapper.getRelation(relation);
            if(relationFound == null) {
                return null;
            }
            log.debug("Found relation: ", relationFound);
        } catch (Exception e) {
            log.warn(e, "Exception when trying to load keyword with name: ", relation);
        } finally {
            session.close();
        }
        return relationFound;
    }

    public List<Relation> getRelationsForKeyword(final Long keyId) {
        final SqlSession session = factory.openSession();
        List<Relation> relationList = null;
        try {
            log.debug("Finding relations matching id: ", keyId);
            final KeywordRelationMapper mapper = session.getMapper(KeywordRelationMapper.class);
            relationList =  mapper.getRelationsForKeyword(keyId);
            if(relationList == null) {
                relationList = Collections.emptyList();
            }
            log.debug("Found relations: ", relationList);
        } catch (Exception e) {
            log.warn(e, "Exception when trying to load relation with id: ", keyId);
        } finally {
            session.close();
        }
        return relationList;
    }

    public List<Relation> getRelationsByTypeForKeyword(Relation relation) {
        final SqlSession session = factory.openSession();
        List<Relation> relationList = null;
        try {
            log.debug("Finding relations matching relation: ", relation);
            final KeywordRelationMapper mapper = session.getMapper(KeywordRelationMapper.class);
            relationList =  mapper.getRelationsByTypeForKeyword(relation);
            if(relationList == null) {
                relationList = Collections.emptyList();
            }
            log.debug("Found relations: ", relationList);
        } catch (Exception e) {
            log.warn(e, "Exception when trying to load relation: ", relation);
        } finally {
            session.close();
        }
        return relationList;
    }

    public void deleteAllRelations() throws SQLException {
        final SqlSession session = factory.openSession();
        try {
            log.debug("Deleting all relations");
            final KeywordRelationMapper mapper = session.getMapper(KeywordRelationMapper.class);
            mapper.deleteAllRelations();
        } catch (Exception e) {
            log.warn(e, "Exception when trying delete relations");
        } finally {
            session.close();
        }
    }

}
