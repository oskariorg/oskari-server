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
import fi.nls.oskari.service.ServiceRuntimeException;
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
        try (final SqlSession session = factory.openSession()) {
            final AnnouncementsMapper mapper = session.getMapper(AnnouncementsMapper.class);
            return parser.parseAnnouncementsMap(mapper.getAdminAnnouncements());
        } catch (Exception e) {
            throw new ServiceRuntimeException("Failed to get admin announcements", e);
        }
    }

    public JSONObject getAnnouncements() {
        try (final SqlSession session = factory.openSession()) {
            final AnnouncementsMapper mapper = session.getMapper(AnnouncementsMapper.class);
            return parser.parseAnnouncementsMap(mapper.getAnnouncements(LocalDate.now()));
        } catch (Exception e) {
            throw new ServiceRuntimeException("Failed to get announcements", e);
        }
    }

    public int updateAnnouncement(final Announcement announcement) {
        int updateId = -1;
        try (final SqlSession session = factory.openSession()) {
            final AnnouncementsMapper mapper = session.getMapper(AnnouncementsMapper.class);
            updateId = mapper.updateAnnouncement(announcement);
            session.commit();
            return updateId;
        } catch (Exception e) {
            throw new ServiceRuntimeException("Failed to update announcements", e);
        }
    }

    public int saveAnnouncement(final Announcement announcement) {
        int saveId = -1;
        try (final SqlSession session = factory.openSession()) {
            final AnnouncementsMapper mapper = session.getMapper(AnnouncementsMapper.class);
            saveId = mapper.saveAnnouncement(announcement);
            session.commit();
            return saveId;
        } catch (Exception e) {
            throw new ServiceRuntimeException("Failed to save announcements", e);
        }
    }

    public int deleteAnnouncement(int id) {
        int deletedId = -1;
        try (final SqlSession session = factory.openSession()) {
            final AnnouncementsMapper mapper = session.getMapper(AnnouncementsMapper.class);
            deletedId = mapper.deleteAnnouncement(id);
            session.commit();
            return deletedId;
        } catch (Exception e) {
            throw new ServiceRuntimeException("Failed to delete announcements", e);
        }
    }
}
