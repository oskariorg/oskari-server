package fi.nls.oskari.mybatis;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import javax.sql.DataSource;

public class MyBatisHelper {

    public static SqlSessionFactory initMyBatis(DataSource ds, Class<?>... mappers) {
        return build(getConfig(ds, mappers));
    }

    public static Configuration getConfig(DataSource ds, Class<?>... mappers) {
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
