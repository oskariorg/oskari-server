package org.oskari.map.userlayer.service;


import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayerData;
import fi.nls.oskari.domain.map.userlayer.UserLayerStyle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.PropertyUtil;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.util.List;

@Oskari
public class UserLayerDbServiceMybatisImpl extends UserLayerDbService {

    private static final Logger log = LogFactory.getLogger(UserLayerDbServiceMybatisImpl.class);
    private static final String USERLAYER_MYBATIS_BATCH_SIZE = "userlayer.mybatis.batch.size";
    final int batchSize = PropertyUtil.getOptional(USERLAYER_MYBATIS_BATCH_SIZE, 1000);
    private SqlSessionFactory factory = null;


    public UserLayerDbServiceMybatisImpl() {
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        final DataSource dataSource = helper.getDataSource(helper.getOskariDataSourceName("userlayer"));

        if (dataSource != null) {
            factory = initializeMyBatis(dataSource);
        } else {
            log.error("Couldn't get datasource for userlayer");
        }
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.getTypeAliasRegistry().registerAlias(UserLayer.class);
        configuration.getTypeAliasRegistry().registerAlias(UserLayerData.class);
        configuration.getTypeAliasRegistry().registerAlias(UserLayerStyle.class);
        configuration.setLazyLoadingEnabled(true);
        configuration.addMapper(UserLayerMapper.class);
        return new SqlSessionFactoryBuilder().build(configuration);
    }

    public int insertUserLayer(final UserLayer userLayer, final UserLayerStyle userLayerStyle, final List<UserLayerData> userLayerDataList) throws ServiceException {
        final SqlSession session = factory.openSession(ExecutorType.BATCH);
        int count = 0;
        try {
            final UserLayerMapper mapper = session.getMapper(UserLayerMapper.class);
            mapper.insertUserLayerStyleRow(userLayerStyle);
            session.flushStatements();
            log.debug("got style id", userLayerStyle.getId());
            userLayer.setStyle_id(userLayerStyle.getId());
            mapper.insertUserLayerRow(userLayer);
            session.flushStatements();
            long userLayerId = userLayer.getId();
            log.debug("got layer id", userLayerId);

            for (UserLayerData userLayerData : userLayerDataList) {
                mapper.insertUserLayerDataRow(userLayerData, userLayerId);
                count++;
                // Flushes batch statements and clears local session cache
                if (count % batchSize == 0) {
                    session.flushStatements();
                    session.clearCache();
                }
            }
            session.flushStatements();
            if (count == 0) throw new ServiceException("no_features");
            log.debug("stored:", count, "rows");
            session.commit();
            return count;
        } catch (Exception e) {
            session.rollback();
            log.error(e, "Rolling back, failed to insert userlayer with id:", +userLayer.getId());
            throw new ServiceException("unable_to_store_data");
        } finally {
            session.close();
        }
    }

    /**
     * update UserLayer table row field mapping
     *
     * @param userLayer
     */
    public int updateUserLayerCols(final UserLayer userLayer) {
        final SqlSession session = factory.openSession();
        try {
            final UserLayerMapper mapper = session.getMapper(UserLayerMapper.class);
            int result = mapper.updateUserLayerCols(userLayer);
            session.commit();
            return result;
        } catch (Exception e) {
            log.error(e, "Failed to update userLayer col mapping", userLayer);
        } finally {
            session.close();
        }
        return 0;
    }

    /**
     * Get UserLayer row  by id
     *
     * @param id
     * @return userLayer object
     */
    public UserLayer getUserLayerById(long id) {
        final SqlSession session = factory.openSession();
        try {
            final UserLayerMapper mapper = session.getMapper(UserLayerMapper.class);
            return mapper.findUserLayer(id);
        } catch (Exception e) {
            log.error(e, "Failed to get userLayer with id:", id);
        } finally {
            session.close();
        }
        return null;
    }


    /**
     * Get UserLayer rows of one user by uuid
     *
     * @param uuid user uuid
     * @return List of userLayer objects
     */
    public List<UserLayer> getUserLayerByUuid(String uuid) {
        final SqlSession session = factory.openSession();
        try {
            final UserLayerMapper mapper = session.getMapper(UserLayerMapper.class);
            return mapper.findUserLayerByUuid(uuid);
        } catch (Exception e) {
            log.error(e, "Failed to get userLayer with uuid:", uuid);
        } finally {
            session.close();
        }
        return null;
    }

    public void deleteUserLayerById(final long id) throws ServiceException {
        final UserLayer userLayer = getUserLayerById(id);
        deleteUserLayer(userLayer);
    }

    public void deleteUserLayer(final UserLayer userLayer) throws ServiceException {
        if (userLayer == null) {
            throw new ServiceException("Tried to delete userLayer with <null> param");
        }
        final SqlSession session = factory.openSession();
        try {
            final UserLayerMapper mapper = session.getMapper(UserLayerMapper.class);
            mapper.deleteUserLayerDataByLayerId(userLayer.getId());
            mapper.deleteUserLayerRow(userLayer.getId());
            mapper.deleteUserLayerStyleRow(userLayer.getStyle_id());
            session.commit();
        } catch (Exception e) {
            session.rollback();
            log.error(e, "Error deleting userLayer with id:", userLayer.getId());
            throw new ServiceException("Error deleting userLayer with id:" + userLayer.getId(), e);
        } finally {
            session.close();
        }
    }

    public void deleteUserLayersByUuid(String uuid) throws ServiceException {
        final SqlSession session = factory.openSession();
        try {
            final UserLayerMapper mapper = session.getMapper(UserLayerMapper.class);
            List<UserLayer> userLayers = mapper.findUserLayerByUuid(uuid);
            if (userLayers.isEmpty()) {
                log.info("Couldn't find any userlayer with uuid:", uuid);
                return;
            }
            for (UserLayer layer : userLayers) {
                deleteUserLayer(layer);
            }
            session.commit();
        } catch (Exception e) {
            session.rollback();
            log.error(e, "Error deleting user related userlayer content with uuid:", uuid);
            throw new ServiceException("Error deleting user related userlayer content with uuid:" + uuid, e);
        } finally {
            session.close();
        }
    }

    /**
     * Updates a userLayer publisher screenName
     *
     * @param id
     * @param uuid
     * @param name
     */
    public int updatePublisherName(final long id, final String uuid, final String name) {
        final SqlSession session = factory.openSession();
        try {
            final UserLayerMapper mapper = session.getMapper(UserLayerMapper.class);
            int result = mapper.updatePublisherName(id, uuid, name);
            session.commit();
            return result;
        } catch (Exception e) {
            log.error(e, "Failed to update publisher name:", name, "id:", id, "uuid", uuid);
        } finally {
            session.close();
        }
        return 0;
    }

    public int updateUserLayerStyleCols(final UserLayerStyle userLayerStyle) {
        final SqlSession session = factory.openSession();
        try {
            final UserLayerMapper mapper = session.getMapper(UserLayerMapper.class);
            int result = mapper.updateUserLayerStyleCols(userLayerStyle);
            session.commit();
            return result;
        } catch (Exception e) {
            log.error(e, "Failed to update userlayerstyle col mapping", userLayerStyle);
        } finally {
            session.close();
        }
        return 0;
    }

    public UserLayerStyle getUserLayerStyleById(long id) {
        final SqlSession session = factory.openSession();
        try {
            final UserLayerMapper mapper = session.getMapper(UserLayerMapper.class);
            return mapper.findUserLayerStyle(id);
        } catch (Exception e) {
            log.error(e, "Failed to get userLayer with id:", id);
        } finally {
            session.close();
        }
        return null;
    }

    /**
     * update UserLayerData table row field mapping
     *
     * @param userLayerData
     */
    public int updateUserLayerDataCols(final UserLayerData userLayerData) {
        final SqlSession session = factory.openSession();
        try {
            final UserLayerMapper mapper = session.getMapper(UserLayerMapper.class);
            int result = mapper.updateUserLayerDataCols(userLayerData);
            return result;
        } catch (Exception e) {
            log.error(e, "Failed to update userlayerdata col mapping", userLayerData);
        } finally {
            session.close();
        }
        return 0;
    }
	
	public String getUserLayerExtent(long id) {
         final SqlSession session = factory.openSession();
         try {
             final UserLayerMapper mapper = session.getMapper(UserLayerMapper.class);
             return mapper.getUserLayerBbox(id);
         } catch (Exception e) {
             log.error(e, "Failed to get userlayer bbox with id:", id);
         } finally {
             session.close();
         }
         return "";
     }

}
