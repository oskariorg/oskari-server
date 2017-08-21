package fi.nls.oskari.mybatis;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

public class MyBatisHelper {

    public static SqlSessionFactory initMyBatis(DataSource ds, Class<?>... mappers) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, ds);
        final Configuration configuration = new Configuration(environment);
        for (Class<?> mapper : mappers) {
            configuration.addMapper(mapper);
        }
        return new SqlSessionFactoryBuilder().build(configuration);
    }

}
