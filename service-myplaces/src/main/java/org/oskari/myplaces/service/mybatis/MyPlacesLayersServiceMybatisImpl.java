package org.oskari.myplaces.service.mybatis;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.JSONObjectMybatisTypeHandler;
import fi.nls.oskari.mybatis.MyBatisHelper;
import fi.nls.oskari.myplaces.service.MyPlacesLayersService;
import fi.nls.oskari.service.ServiceException;

/**
 * MyBatis implementation of MyPlacesLayersService
 * For non-direct database use
 * @see fi.nls.oskari.myplaces.service.wfst.MyPlacesLayersServiceWFST
 */
public class MyPlacesLayersServiceMybatisImpl implements MyPlacesLayersService {

    private static final Logger LOG = LogFactory.getLogger(MyPlacesLayersServiceMybatisImpl.class);

    private SqlSessionFactory factory;

    public MyPlacesLayersServiceMybatisImpl() {
        this(DatasourceHelper.getInstance().getDataSource(DatasourceHelper.getInstance().getOskariDataSourceName("myplaces")));
    }

    public MyPlacesLayersServiceMybatisImpl(DataSource dataSource) {
        if (dataSource == null) {
            LOG.error("Couldn't get datasource for userlayer");
        } else {
            this.factory = initializeMyBatis(dataSource);
        }
    }

    private SqlSessionFactory initializeMyBatis(DataSource ds) {
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("development", transactionFactory, ds);
        Configuration configuration = new Configuration(environment);
        configuration.getTypeAliasRegistry().registerAlias(MyPlaceCategory.class);
        configuration.getTypeHandlerRegistry().register(JSONObjectMybatisTypeHandler.class);
        configuration.addMapper(MyPlaceCategoryMapper.class);
        return MyBatisHelper.build(configuration);
    }

    private MyPlaceCategoryMapper getMapper(SqlSession session) {
        return session.getMapper(MyPlaceCategoryMapper.class);
    }

    @Override
    public Optional<MyPlaceCategory> getById(long id) throws ServiceException {
        try (SqlSession session = factory.openSession()) {
            return Optional.ofNullable(getMapper(session).getById(id));
        }
    }

    @Override
    public List<MyPlaceCategory> getByIds(long[] ids) throws ServiceException {
        try (SqlSession session = factory.openSession()) {
            MyPlaceCategoryMapper mapper = getMapper(session);
            return Arrays.stream(ids)
                    .mapToObj(id -> mapper.getById(id))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<MyPlaceCategory> getByUserId(String uuid) throws ServiceException {
        try (SqlSession session = factory.openSession()) {
            return getMapper(session).getByUserId(uuid);
        }
    }

    @Override
    public int insert(List<MyPlaceCategory> categories) throws ServiceException {
        try (SqlSession session = factory.openSession(ExecutorType.BATCH)) {
            MyPlaceCategoryMapper mapper = getMapper(session);
            int n = 0;
            for (MyPlaceCategory category : categories) {
                mapper.insert(category);
                n++;
            }
            session.commit();
            return n;
        }
    }

    @Override
    public int update(List<MyPlaceCategory> categories) throws ServiceException {
        try (SqlSession session = factory.openSession(ExecutorType.BATCH)) {
            MyPlaceCategoryMapper mapper = getMapper(session);
            int n = 0;
            for (MyPlaceCategory category : categories) {
                mapper.update(category);
                n++;
            }
            session.commit();
            return n;
        }
    }

    @Override
    public int delete(long[] ids) throws ServiceException {
        try (SqlSession session = factory.openSession(ExecutorType.BATCH)) {
            MyPlaceCategoryMapper mapper = getMapper(session);
            int n = 0;
            for (long id : ids) {
                n += mapper.delete(id);
            }
            session.commit();
            return n;
        }
    }

}
