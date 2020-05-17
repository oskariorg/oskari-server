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
            // TODO: anything better to detect that we are in test env?
            ds = getTestDS();
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

    private static DataSource getTestDS() {
        try {
            // try to dig up TestHelper that is only available while testing to get a mem-based datasource
            Class helper = Class.forName("fi.nls.test.util.TestHelper");
            Method m = helper.getMethod("createMemDBforUnitTest", List.class);
            // TODO: call with DB initializing scripts to actually use this
            return (DataSource) m.invoke(null, (Object) Collections.emptyList());
        } catch (Exception e) {
            throw new RuntimeException("Not testing");
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
