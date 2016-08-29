package fi.nls.oskari.rating;

import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

/**
 * Created by MHURME on 11.9.2015.
 */
public class RatingServiceMybatisImpl extends RatingService {

    private static final Logger LOG = LogFactory.getLogger(
            RatingServiceMybatisImpl.class);

    private SqlSessionFactory factory = null;

    public RatingServiceMybatisImpl() {

        final DatasourceHelper helper = DatasourceHelper.getInstance();
        final DataSource dataSource = helper.getDataSource(helper.getOskariDataSourceName("ratings"));
        if(dataSource != null) {
            factory = initializeMyBatis(dataSource);
        }
        else {
            LOG.error("Couldn't get datasource for ratings");
        }
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.getTypeAliasRegistry().registerAlias(Rating.class);
        configuration.setLazyLoadingEnabled(true);
        configuration.addMapper(RatingMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }

    public Rating saveRating(Rating rating) {
        if (rating.getId() == 0) {
             return insertRating(rating);
        } else {
            return updateRating(rating);
        }
    }

    public boolean validateRequiredStrings(Rating rating) {
        if (rating.getCategory().isEmpty()) return false;
        if (rating.getCategoryItem().isEmpty()) return false;
        return true;
    }

    private Rating insertRating(Rating rating) {
        final SqlSession session = factory.openSession();
        try {
            final RatingMapper mapper = session.getMapper(RatingMapper.class);
            mapper.insertRating(rating);
            session.commit();
            return rating;
        } catch (Exception e) {
            LOG.warn(e, "Exception when trying to insert new rating");
        } finally {
            session.close();
        }
        return rating;
    }

    private Rating updateRating(Rating rating) {
        final SqlSession session = factory.openSession();
        try {
            final RatingMapper mapper = session.getMapper(RatingMapper.class);
            mapper.updateRating(rating);
            session.commit();
            return findRating(rating.getId());
        } catch (Exception e) {
            LOG.warn(e, "Exception when trying to update rating");
        } finally {
            session.close();
        }
        return rating;
    }

    private Rating findRating(long id) {
        final SqlSession session = factory.openSession();
        try {
            final RatingMapper mapper = session.getMapper(RatingMapper.class);
            return mapper.find(id);
        } catch (Exception e) {
            LOG.error(e, "Failed to load rating");
        } finally {
            session.close();
        }
        return new Rating();
    }

    public List<Rating> getAllRatingsFor(String category, String categoryItem) {
        final SqlSession session = factory.openSession();
        try {
            final RatingMapper mapper = session.getMapper(RatingMapper.class);
            return mapper.findAllFor(category, categoryItem);
        } catch (Exception e) {
            LOG.error(e, "Failed to load ratings");
        } finally {
            session.close();
        }
        return Collections.emptyList();
    }

    public String[] getAverageRatingFor(String category, String categoryItem) {
        List<Rating> ratings = getAllRatingsFor(category, categoryItem);
        if (ratings.isEmpty()) {
            String[] result = {"0","0"};
            return result;
        }
        int amount = 0;
        int result = 0;
        for(Rating rating: ratings) {
            result += rating.getRating();
            amount++;
        }
        String[] average = {Integer.toString(result / amount), Integer.toString(amount)};
        return average;
    }

    public String findLatestAdminRating(String category, String categoryItem, String adminRole) {
        final SqlSession session = factory.openSession();
        try {
            final RatingMapper mapper = session.getMapper(RatingMapper.class);
            return mapper.findLatestAdminRating(category, categoryItem, adminRole);
        } catch (Exception e) {
            LOG.error(e, "Failed to load ratings");
        } finally {
            session.close();
        }
        return null;
    }

}
