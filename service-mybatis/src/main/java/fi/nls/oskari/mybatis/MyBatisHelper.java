package fi.nls.oskari.mybatis;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class MyBatisHelper {

    public static SqlSessionFactory initMyBatis(DataSource ds, Class<?>... mappers) {
        return build(getConfig(ds, mappers));
    }

    public static Configuration getConfig(DataSource ds, Class<?>... mappers) {
        if (ds == null) {
            // ds = getTestDS();
        }
        if (ds == null) {
            throw new NullPointerException("Tried initializing MyBatis without a datasource");
        }
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, ds);
        final Configuration configuration = new Configuration(environment);
        configuration.setLazyLoadingEnabled(true);
        addMappers(configuration, mappers);
        return configuration;
    }

    /**
     * Work in progress. This could be used to init mem-based database for unit tests.
     * @return
     */
    private static DataSource getTestDS() {
        try {
            // try to dig up TestHelper that is only available while testing to get a mem-based datasource
            Class helper = Class.forName("fi.nls.test.util.TestHelper");
            Method m = helper.getMethod("createMemDBforUnitTest", List.class);
            return (DataSource) m.invoke(null, (Object) Collections.emptyList());
        } catch (Exception e) {
            throw new RuntimeException("Tried to create mem-based db for testing but test libraries not in classpath");
        }
    }

    public static void addAliases(Configuration config, Class<?>... aliases) {
        for (Class<?> alias : aliases) {
            config.getTypeAliasRegistry().registerAlias(alias);
        }
    }

    public static void addMappers(Configuration config, Class<?>... mappers) {
        for (Class<?> mapper : mappers) {
            config.addMapper(mapper);
        }
    }

    public static SqlSessionFactory build(Configuration config) {
        return new SqlSessionFactoryBuilder().build(config);
    }
}
