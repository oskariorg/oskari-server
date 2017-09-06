package fi.nls.oskari.mybatis;

import javax.sql.DataSource;

import fi.nls.oskari.service.ServiceRuntimeException;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

public class MyBatisHelper {

    public static SqlSessionFactory initMyBatis(DataSource ds, Class<?>... mappers) {
        return build(getConfig(ds, mappers));
    }

    public static Configuration getConfig(DataSource ds, Class<?>... mappers) {
        if(ds == null) {
            throw new ServiceRuntimeException("Tried initializing MyBatis without a datasource");
        }
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, ds);
        final Configuration configuration = new Configuration(environment);
        configuration.setLazyLoadingEnabled(true);
        for (Class<?> mapper : mappers) {
            configuration.addMapper(mapper);
        }
        return configuration;
    }

    public static void attachAliases(Configuration config, Class<?>... aliases) {
        for (Class<?> alias : aliases) {
            config.getTypeAliasRegistry().registerAlias(alias);
        }
    }

    public static SqlSessionFactory build(Configuration config) {
        return new SqlSessionFactoryBuilder().build(config);
    }
}
