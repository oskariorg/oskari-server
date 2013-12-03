package fi.nls.oskari.db;

import com.ibatis.sqlmap.engine.datasource.DataSourceFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Dummy wrapper for getting existing ibatis configs to work with standalone DBHandler
 * @author SMAKINEN
 */
public class IbatisDataSourceFactory implements DataSourceFactory {
    @Override
    public void initialize(Map map) {
    }

    @Override
    public DataSource getDataSource() {
        DataSource source = new DataSource() {
            @Override
            public Connection getConnection() throws SQLException {
                return DBHandler.getConnection();
            }

            @Override
            public Connection getConnection(String username, String password) throws SQLException {
                return getConnection();
            }

            @Override
            public PrintWriter getLogWriter() throws SQLException {
                return null;
            }

            @Override
            public void setLogWriter(PrintWriter out) throws SQLException {
            }

            @Override
            public void setLoginTimeout(int seconds) throws SQLException {

            }

            @Override
            public int getLoginTimeout() throws SQLException {
                return 0;
            }

            @Override
            public <T> T unwrap(Class<T> iface) throws SQLException {
                return null;
            }

            @Override
            public boolean isWrapperFor(Class<?> iface) throws SQLException {
                return false;
            }

            // required for java 7
            public Logger getParentLogger()
                    throws SQLFeatureNotSupportedException {
                throw new SQLFeatureNotSupportedException("Not implemented");
            }
        };
        return source;
    }
}
