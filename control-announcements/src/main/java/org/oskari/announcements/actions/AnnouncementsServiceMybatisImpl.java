package org.oskari.announcements.actions;

import javax.sql.DataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.db.DatasourceHelper;
import org.oskari.announcements.helpers.Announcement;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.json.JSONObject;
import org.oskari.announcements.helpers.AnnouncementsParser;
import org.oskari.announcements.mappers.AnnouncementsMapper;
import java.time.LocalDate;

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
        JSONObject data = new JSONObject();
        try {
            final AnnouncementsMapper mapper = session.getMapper(AnnouncementsMapper.class);
            data = parser.parseAnnouncementsMap(mapper.getAnnouncements(LocalDate.now()));
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
        int updateId = -1;
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
        final SqlSession session = factory.openSession();
        int saveId = -1;
        try {
            final AnnouncementsMapper mapper = session.getMapper(AnnouncementsMapper.class);
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
