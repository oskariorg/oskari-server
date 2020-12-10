package org.oskari.announcements.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.map.DataProvider;
import org.oskari.announcements.helpers.Announcement;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.mybatis.JSONObjectMybatisTypeHandler;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;
import org.oskari.announcements.helpers.AnnouncementsParser;
import org.oskari.announcements.mappers.AnnouncementsMapper;

@Oskari
public class AnnouncementsServiceMybatisImpl extends AnnouncementsService{

    private static final Logger LOG = LogFactory.getLogger(AnnouncementsServiceMybatisImpl.class);

    private SqlSessionFactory factory = null;
    private AnnouncementsParser parser = new AnnouncementsParser();

    public AnnouncementsServiceMybatisImpl() {
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        DataSource dataSource = helper.getDataSource();
        if (dataSource == null) {
            dataSource = helper.createDataSource();
        }
        if (dataSource == null) {
            LOG.error("Couldn't get datasource for oskari announcements service");
        }
        factory = initializeMyBatis(dataSource);
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.getTypeAliasRegistry().registerAlias(AnnouncementsService.class);
        configuration.setLazyLoadingEnabled(true);
        configuration.addMapper(AnnouncementsMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }

    public JSONObject getAdminAnnouncements() {
        LOG.debug("get admin announcements");
        final SqlSession session = factory.openSession();
        JSONObject data = new JSONObject();
        try {
            final AnnouncementsMapper mapper = session.getMapper(AnnouncementsMapper.class);
            System.out.println("============================================================");
            System.out.println(mapper);
            data = parser.parseAnnouncementsMap(mapper.getAdminAnnouncements());
            session.commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get admin announcements", e);
        } finally {
            session.close();
        }
        return data;
    }

    public JSONObject getAnnouncements() {
        LOG.debug("get announcements");
        final SqlSession session = factory.openSession();
        System.out.println("============================================================");
        System.out.println(factory);
        JSONObject data = new JSONObject();
        try {
            final AnnouncementsMapper mapper = session.getMapper(AnnouncementsMapper.class);
            System.out.println("============================================================");
            System.out.println(mapper);
            data = parser.parseAnnouncementsMap(mapper.getAnnouncements());
            session.commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get announcements", e);
        } finally {
            session.close();
        }
        return data;
    }

    public int updateAnnouncement(final Announcement announcement) {
        LOG.debug("update announcement");
        final SqlSession session = factory.openSession();
        int updateId;
        try {
            final AnnouncementsMapper mapper = session.getMapper(AnnouncementsMapper.class);
            updateId = mapper.updateAnnouncement(announcement);
            session.commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update announcement", e);
        } finally {
            session.close();
        }
        return updateId;
    }

    public int saveAnnouncement(final Announcement announcement) {
        LOG.debug("save new announcement");
        System.out.println("============================================================");
        final SqlSession session = factory.openSession();
        int saveId;
        try {
            System.out.println("======TRY==========");
            final AnnouncementsMapper mapper = session.getMapper(AnnouncementsMapper.class);
            System.out.println("============================================================");
            System.out.println(mapper);
            saveId = mapper.saveAnnouncement(announcement);
            session.commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save announcement", e);
        } finally {
            session.close();
        }
        return saveId;
    }

    public int deleteAnnouncement(int id) {
        LOG.debug("delete announcement with id: " + id);
        final SqlSession session = factory.openSession();
        int deletedId = -1;
        try {
            final AnnouncementsMapper mapper = session.getMapper(AnnouncementsMapper.class);
            deletedId = mapper.deleteAnnouncement(id);
            session.commit();
        } catch (Exception e) {
            LOG.error(e, "Couldn't delete announcement with id:", id);
        } finally {
            session.close();
        }
        return deletedId;
    }
}
