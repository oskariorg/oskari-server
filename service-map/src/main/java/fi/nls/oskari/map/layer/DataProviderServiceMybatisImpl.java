package fi.nls.oskari.map.layer;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.DataProvider;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.MyBatisHelper;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Oskari
public class DataProviderServiceMybatisImpl extends DataProviderService {

    private static final Logger log = LogFactory.getLogger(DataProviderServiceMybatisImpl.class);
    // FIXME: use some caching lib for this, and clear cache on update/delete etc
    final private static Map<Integer, DataProvider> ID_CACHE = new HashMap<Integer, DataProvider>();

    private SqlSessionFactory factory = null;

    public DataProviderServiceMybatisImpl() {
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        DataSource dataSource = helper.getDataSource();
        if (dataSource == null) {
            dataSource = helper.createDataSource();
        }
        if (dataSource == null) {
            log.error("Couldn't get datasource for data provider service");
        }
        factory = initializeMyBatis(dataSource);
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final Configuration configuration = MyBatisHelper.getConfig(dataSource);
        MyBatisHelper.addAliases(configuration, DataProvider.class);
        MyBatisHelper.addMappers(configuration, DataProviderMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }

    public boolean hasPermissionToUpdate(final User user, final int layerId) {

        // TODO: check against permissions
        if (!user.isAdmin()) {
            return false;
        }
        if (layerId <= -1) {
            return false;
        }
        // TODO: maybe check if we have a layer with given id in DB
        return true;
    }

    public DataProvider find(int id) {
        log.debug("Find data provider with id: " + id);
        if(id == -1) {
            return null;
        }
        DataProvider group = ID_CACHE.get(id);
        if(group != null) {
            return group;
        }
        final SqlSession session = factory.openSession();
        try {
            final DataProviderMapper mapper = session.getMapper(DataProviderMapper.class);
            group = mapper.find(id);
            if(group != null) {
                ID_CACHE.put(group.getId(), group);
            }
            return mapper.find(id);
        } catch (Exception e) {
            log.warn("Unable to find data provider with id :" + id);
        } finally {
            session.close();
        }
        return group;
    }

    /**
     * Returns first group (searched in arbitratry order) that has any part
     * of the group name in any language(!) matching the given name-parameter.
     * Use carefully and preferrably with long parameter name.
     * FIXME: Quick and dirty
     * @param name
     * @return matching group or null if no match
     */
    public DataProvider findByName(final String name) {
        log.debug("Find data provider by name: " + name);
        final List<DataProvider> groups = findAll();
        for(DataProvider group : groups) {
            if(group.getLocale().toString().indexOf(name) > -1) {
                return group;
            }
        }
        return null;
    }

    public List<DataProvider> findAll() {
        log.debug("Find all dataproviders");
        final SqlSession session = factory.openSession();
        List<DataProvider> groups = new ArrayList<>();
        try {
            final DataProviderMapper mapper = session.getMapper(DataProviderMapper.class);
            groups = mapper.findAll();
            for(DataProvider group : groups) {
                ID_CACHE.put(group.getId(), group);
            }
        } catch (Exception e) {
            log.warn("Unable to find data providers");
        } finally {
            session.close();
        }
        return groups;
    }

    public void delete(int id) {
        log.debug(("Delete data provider with id: "+ id));
        final SqlSession session = factory.openSession();
        try {
            final DataProviderMapper mapper = session.getMapper(DataProviderMapper.class);
            ID_CACHE.remove(id);
            mapper.delete(id);
            session.commit();
        } catch (Exception e) {
            log.warn("Unable to delete data provider with id: " + id);
        } finally {
            session.close();
        }
    }

    public void update(final DataProvider group) {
        log.debug("Update data provider with id: " + group.getId());
        final SqlSession session = factory.openSession();
        try {
            final DataProviderMapper mapper = session.getMapper(DataProviderMapper.class);
            mapper.update(group.getLocale(), group.getId());
            session.commit();
            ID_CACHE.put(group.getId(), group);
        } catch (Exception e) {
            log.warn("Unable to update data provider with id: " + group.getId());
        } finally {
            session.close();
        }
    }

    public int insert(DataProvider dataProvider) {
        log.debug("Insert data provider");
        final SqlSession session = factory.openSession();
        try {
            final DataProviderMapper mapper = session.getMapper(DataProviderMapper.class);
            mapper.insert(dataProvider);
            session.commit();
        } catch (Exception e) {
            log.warn("Unable to insert data provider");
        } finally {
            session.close();
        }
        return dataProvider.getId();
    }
}
