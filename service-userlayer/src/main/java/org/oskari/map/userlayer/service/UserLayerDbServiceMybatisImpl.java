package org.oskari.map.userlayer.service;


import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayerData;
import fi.nls.oskari.domain.map.UserDataStyle;
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
import java.util.Optional;

@Oskari
public class UserLayerDbServiceMybatisImpl extends UserLayerDbService {

    private static final Logger log = LogFactory.getLogger(UserLayerDbServiceMybatisImpl.class);
    private static final String USERLAYER_MYBATIS_BATCH_SIZE = "userlayer.mybatis.batch.size";
    final int batchSize = PropertyUtil.getOptional(USERLAYER_MYBATIS_BATCH_SIZE, 1000);
    private final Cache<UserLayer> cache;
    private SqlSessionFactory factory = null;


    public UserLayerDbServiceMybatisImpl() {
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        final DataSource dataSource = helper.getDataSource(helper.getOskariDataSourceName("userlayer"));

        if (dataSource != null) {
            factory = initializeMyBatis(dataSource);
        } else {
            log.error("Couldn't get datasource for userlayer");
        }
        cache = CacheManager.getCache(getClass().getName());
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.getTypeAliasRegistry().registerAlias(UserLayer.class);
        configuration.getTypeAliasRegistry().registerAlias(UserLayerData.class);
        configuration.getTypeAliasRegistry().registerAlias(UserDataStyle.class);
        configuration.setLazyLoadingEnabled(true);
        configuration.addMapper(UserLayerMapper.class);
        return new SqlSessionFactoryBuilder().build(configuration);
    }

    public int insertUserLayer(final UserLayer userLayer, final List<UserLayerData> userLayerDataList) throws UserLayerException {
        try (SqlSession session = factory.openSession(ExecutorType.BATCH)) {
            int count = 0;
            final UserLayerMapper mapper = getMapper(session);
            UserDataStyle userLayerStyle = userLayer.getStyle();
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
            if (count == 0) throw new UserLayerException("UserLayer doesn't contain features", UserLayerException.ErrorType.NO_FEATURES);
            log.debug("stored:", count, "rows");
            session.commit();
            cache(userLayer);
            return count;
        } catch (Exception e) {
            log.error(e, "Rolling back, failed to insert userlayer with id:", +userLayer.getId());
            if(e instanceof UserLayerException){
                throw e; // no features
            }
            throw new UserLayerException("Failed to store features to database", UserLayerException.ErrorType.STORE);
        }
    }

    /**
     * update UserLayer table row field mapping
     *
     * @param userLayer
     */
    public int updateUserLayerCols(final UserLayer userLayer) {
        try (SqlSession session = factory.openSession()) {
            final UserLayerMapper mapper = getMapper(session);
            int result = mapper.updateUserLayerCols(userLayer);
            session.commit();
            return result;
        } catch (Exception e) {
            log.error(e, "Failed to update userLayer col mapping", userLayer);
            return 0;
        }
    }

    private UserLayer getFromCache(long id) {
        return cache.get(Long.toString(id));
    }

    private UserLayer cache(UserLayer layer) {
        if (layer != null) {
            cache.put(Long.toString(layer.getId()), layer);
        }
        return layer;
    }

    /**
     * Get UserLayer row  by id
     *
     * @param id
     * @return userLayer object
     */
    public UserLayer getUserLayerById(long id) {
        UserLayer layer = getFromCache(id);
        if (layer != null) {
            return layer;
        }
        try (SqlSession session = factory.openSession()) {
            layer = getMapper(session).findUserLayer(id);
            return cache(layer);
        } catch (Exception e) {
            log.error(e, "Failed to get userLayer with id:", id);
            return null;
        }
    }


    /**
     * Get UserLayer rows of one user by uuid
     *
     * @param uuid user uuid
     * @return List of userLayer objects
     */
    public List<UserLayer> getUserLayerByUuid(String uuid) {
        try (SqlSession session = factory.openSession()) {
            return getMapper(session).findUserLayerByUuid(uuid);
        } catch (Exception e) {
            log.error(e, "Failed to get userLayer with uuid:", uuid);
            return null;
        }
    }

    public void deleteUserLayerById(final long id) throws ServiceException {
        final UserLayer userLayer = getUserLayerById(id);
        deleteUserLayer(userLayer);
        cache.remove(Long.toString(id));
    }

    public void deleteUserLayer(final UserLayer userLayer) throws ServiceException {
        if (userLayer == null) {
            throw new ServiceException("Tried to delete userLayer with <null> param");
        }
        try (SqlSession session = factory.openSession()) {
            final UserLayerMapper mapper = getMapper(session);
            mapper.deleteUserLayerDataByLayerId(userLayer.getId());
            mapper.deleteUserLayerRow(userLayer.getId());
            mapper.deleteUserLayerStyleRow(userLayer.getStyle_id());
            session.commit();
            cache.remove(Long.toString(userLayer.getId()));
        } catch (Exception e) {
            log.error(e, "Error deleting userLayer with id:", userLayer.getId());
            throw new ServiceException("Error deleting userLayer with id:" + userLayer.getId(), e);
        }
    }

    public void deleteUserLayersByUuid(String uuid) throws ServiceException {
        try (SqlSession session = factory.openSession()) {
            final UserLayerMapper mapper = getMapper(session);
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
            log.error(e, "Error deleting user related userlayer content with uuid:", uuid);
            throw new ServiceException("Error deleting user related userlayer content with uuid:" + uuid, e);
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
        try (SqlSession session = factory.openSession()) {
            final UserLayerMapper mapper = getMapper(session);
            int result = mapper.updatePublisherName(id, uuid, name);
            session.commit();
            // update data in cache
            UserLayer layer = getFromCache(id);
            if (layer != null) {
                layer.setPublisher_name(name);
            }
            return result;
        } catch (Exception e) {
            log.error(e, "Failed to update publisher name:", name, "id:", id, "uuid", uuid);
            return 0;
        }
    }

    public int updateUserLayerStyleCols(final UserDataStyle userLayerStyle) {
        try (SqlSession session = factory.openSession()) {
            final UserLayerMapper mapper = getMapper(session);
            int result = mapper.updateUserLayerStyleCols(userLayerStyle);
            session.commit();
            return result;
        } catch (Exception e) {
            log.error(e, "Failed to update userlayerstyle col mapping", userLayerStyle);
            return 0;
        }
    }

    public UserDataStyle getUserLayerStyleById(long id) {
        try (SqlSession session = factory.openSession()) {
            return getMapper(session).findUserLayerStyle(id);
        } catch (Exception e) {
            log.error(e, "Failed to get userLayer with id:", id);
            return null;
        }
    }

    /**
     * update UserLayerData table row field mapping
     *
     * @param userLayerData
     */
    public int updateUserLayerDataCols(final UserLayerData userLayerData) {
        try (SqlSession session = factory.openSession()) {
            return getMapper(session).updateUserLayerDataCols(userLayerData);
        } catch (Exception e) {
            log.error(e, "Failed to update userlayerdata col mapping", userLayerData);
            return 0;
        }
    }
	
	public String getUserLayerExtent(long id) {
	    try (SqlSession session = factory.openSession()) {
             return getMapper(session).getUserLayerBbox(id);
         } catch (Exception e) {
             log.error(e, "Failed to get userlayer bbox with id:", id);
             return "";
         }
     }

	private UserLayerMapper getMapper(SqlSession session) {
	    return session.getMapper(UserLayerMapper.class);
	}

}
