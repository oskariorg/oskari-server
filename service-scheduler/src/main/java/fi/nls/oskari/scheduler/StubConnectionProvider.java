package fi.nls.oskari.scheduler;

import org.quartz.utils.ConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Because both Liferay and our custom map portlet need to use Quartz for scheduling,
 * but unfortunately Liferay provides its own Quartz configuration in its portal.properties,
 * which we can only partially override in our portlet.properties (we can't remove properties
 * Liferay has already set, but we can override them with stub values), we need these stub
 * classes.
 */
public class StubConnectionProvider implements ConnectionProvider {

    @Override
    public Connection getConnection() throws SQLException {
        return null;
    }

    @Override
    public void shutdown() throws SQLException {
    }

    @Override
    public void initialize() throws SQLException {
    }

}
