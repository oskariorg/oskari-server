package fi.nls.oskari.service.db;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import com.ibatis.sqlmap.client.SqlMapSession;
import fi.nls.oskari.service.OskariComponent;

import java.io.Reader;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Copy of BaseIbatisService having OskariComponent API
 * @param <E>
 */
public abstract class OskariComponentIbatisService<E> extends OskariComponent {

    private SqlMapClient client = null;
    // make it static so we can change this with one call to all services when needed
    private static String SQL_MAP_LOCATION = "META-INF/SqlMapConfig.xml";

    /** Override this and return ibatis namespace */
    protected abstract String getNameSpace();

    /**
     * Returns SQLmap
     * 
     * @return
     */
    protected SqlMapClient getSqlMapClient() {
        if (client != null) {
            return client;
        }

        Reader reader = null;
        try {
            String sqlMapLocation = getSqlMapLocation();
            reader = Resources.getResourceAsReader(sqlMapLocation);
            client = SqlMapClientBuilder.buildSqlMapClient(reader);
            return client;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve SQL client", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Static setter to override default location
     * @param newLocation
     */
    public static void setSqlMapLocation(final String newLocation) {
        SQL_MAP_LOCATION = newLocation;
    }
    /*
     * The purpose of this method is to allow many SqlMapConfig.xml files in a
     * single portlet
     */
    protected String getSqlMapLocation() {
        return SQL_MAP_LOCATION;
    }

    /**
     * Opens a new session for transaction. REMEMBER TO CLOSE IT WITH
     * endSession(); in finally block.
     */
    protected SqlMapSession openSession() {
        SqlMapClient client = getSqlMapClient();
        SqlMapSession session = client.openSession();
        return session;
    }

    /**
     * Ends given session opened with openSession().
     * 
     * @return true if success/false if problems closing
     */
    protected boolean endSession(final SqlMapSession session) {
        try {
            session.endTransaction();
        } catch (Exception ignored) {
            return false;
        } finally {
            try {
                // MUST be closed if explicitly opened (via openSession()).
                session.close();
            } catch (Exception ignored) {
            }
        }
        return true;
    }

    /**
     * Queries for list
     * 
     * @param sqlId
     * @return List of type E
     */
    @SuppressWarnings("unchecked")
    public List<E> queryForList(String sqlId) {
        try {
            client = getSqlMapClient();
            List<E> results = client.queryForList(sqlId);
            return results;
        } catch (Exception e) {
        	System.out.println(e.getMessage());
            throw new RuntimeException("Failed to query", e);
        }
    }

    /**
     * Queries for list with given param object
     * 
     * @param sqlId
     * @param o objectIdentifier
     * @return
     */
    @SuppressWarnings("unchecked")
    public <F> List<F> queryForList(String sqlId, Object o) {
        return (List<F>) queryForRawList(sqlId, o);
    }

    public List<Object> queryForRawList(String sqlId, Object o) {
        try {
            client = getSqlMapClient();
            return client.queryForList(sqlId, o);
        } catch (Exception e) {
            throw new RuntimeException("Failed to query", e);
        }
    }

    /**
     * Queries for object
     * 
     * @param sqlId
     * @param objectIdentifier
     * @return type E
     */
    @SuppressWarnings("unchecked")
    public E queryForObject(String sqlId, int objectIdentifier) {
        try {
            client = getSqlMapClient();
            E result = (E) client.queryForObject(sqlId, objectIdentifier);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to query", e);
        }
    }

    /**
     * Queries for object
     * 
     * @param sqlId
     * @param objectIdentifier
     * @return type E
     */
    @SuppressWarnings("unchecked")
    public E queryForObject(String sqlId, String objectIdentifier) {
        try {
            return (E) queryForRawObject(sqlId, objectIdentifier);
        } catch (Exception e) {
            throw new RuntimeException("Failed to query", e);
        }
    }

    public Object queryForRawObject(String sqlId, String objectIdentifier) {
        try {
            client = getSqlMapClient();
            return client.queryForObject(sqlId, objectIdentifier);
        } catch (Exception e) {
            throw new RuntimeException("Failed to query", e);
        }
    }
    public Object queryForRawObject(String sqlId, Object o) {
        try {
            client = getSqlMapClient();
            return client.queryForObject(sqlId, o);
        } catch (Exception e) {
            throw new RuntimeException("Failed to query", e);
        }
    }

    /**
     * Queries for object
     * 
     * @param sqlId
     * @param o object
     * @return type F
     */
    @SuppressWarnings("unchecked")
    public <F> F queryForObject(String sqlId, Object o) {
        try {
            return (F) queryForRawObject(sqlId, o);
        } catch (Exception e) {
            throw new RuntimeException("Failed to query", e);
        }
    }

    /**
     * Does an insert. Transaction is manually handled here because we use query
     * style insert because we want to return unique id after insert.
     * 
     * This used to be implemented with Postreges "returning" clause e.g.
     * 
     * insert into portti_xxx (nameFi, nameSv, nameEn) values (#nameFi#,
     * #nameSv#, #nameEn#) returning id
     * 
     * but unfortunately we have postreges 8.1 in production and during release
     * 2.0 we had to time to upgrade it to newer version. "Returning" -clause is
     * introduced for postgres in version 8.2, so we will need at least that in
     * order to use it.
     * 
     * From today on (3.5.2010) implementation will be synchorized method that
     * first makes an insert and after that makes a query
     * 
     * @param sqlId
     * @param o
     * @return created unique id
     */
    public synchronized int insert(String sqlId, E o) {
        SqlMapClient client = null;
        try {
            client = getSqlMapClient();
            client.startTransaction();
            client.insert(sqlId, o);
            Integer id = (Integer) client.queryForObject(getNameSpace()
                    + ".maxId");
            client.commitTransaction();
            return id;
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert", e);
        } finally {
            if (client != null) {
                try {
                    client.endTransaction();
                } catch (SQLException e) {
                    // forget
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized int insert(String sqlId, Map o) {
        SqlMapClient client = null;
        try {
            client = getSqlMapClient();
            client.startTransaction();
            client.insert(sqlId, o);
            Integer id = (Integer) client.queryForObject(getNameSpace()
                    + ".maxId");
            client.commitTransaction();
            return id;
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert", e);
        } finally {
            if (client != null) {
                try {
                    client.endTransaction();
                } catch (SQLException e) {
                    // forget
                }
            }
        }
    }

    public synchronized <F> void insert(String sqlId, List<F> l) {
        SqlMapClient client = null;
        try {
            client = getSqlMapClient();
            client.startTransaction();

            for (F o : l) {
                client.insert(sqlId, o);
            }

            client.commitTransaction();
            return;
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert", e);
        } finally {
            if (client != null) {
                try {
                    client.endTransaction();
                } catch (SQLException e) {
                    // forget
                }
            }
        }
    }

    /**
     * Updates object
     * 
     * @param sqlId
     * @param o
     */
    public void update(String sqlId, E o) {
        try {
            getSqlMapClient().update(sqlId, o);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update", e);
        }
    }

    public void update(String sqlId, Map<String, String> parameterMap) {
        try {
            getSqlMapClient().update(sqlId, parameterMap);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update", e);
        }
    }

    /**
     * Deletes object
     * 
     * @param sqlId
     * @param objectId
     */
    public void delete(String sqlId, int objectId) {
        try {
            getSqlMapClient().delete(sqlId, objectId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete", e);
        }
    }

    /**
     * Deletes object
     * 
     * @param sqlId
     * @param objectId
     */
    public void delete(String sqlId, long objectId) {
        try {
            getSqlMapClient().delete(sqlId, objectId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete", e);
        }
    }

    /**
     * Deletes object
     * 
     * @param sqlId
     * @param parameterMap
     */
    public void delete(String sqlId, Map<String, String> parameterMap) {
        try {
            getSqlMapClient().delete(sqlId, parameterMap);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete", e);
        }
    }

    /**
     * Parameterized type for finding one object
     */
    public E find(int id) {
        return queryForObject(getNameSpace() + ".find", id);
    }

    /**
     * Parameterized type for finding one object
     */
    public E find(String id) {
        return queryForObject(getNameSpace() + ".find", id);
    }

    /**
     * Parameterized type for finding all Objects
     */
    public List<E> findAll() {
        return queryForList(getNameSpace() + ".findAll");
    }
    
    /**
     * Parameterized type for finding all WMS Objects
     */
    public List<E> findAllWMS() {
        return queryForList(getNameSpace() + ".findAllWMS");
    }

    /**
     * Parameterized type for updating object
     */
    public void update(E o) {
        update(getNameSpace() + ".update", o);
    }

    /**
     * Parameterized type for inserting object
     */
    public int insert(E o) {
        return insert(getNameSpace() + ".insert", o);
    }

    /**
     * Parameterized type for deleting object
     */
    public void delete(int id) {
        delete(getNameSpace() + ".delete", id);
    }

    /**
     * Parameterized type for deleting object
     */
    public void delete(Map<String, String> parameterMap) {
        delete(getNameSpace() + ".delete", parameterMap);
    }
    
    
    public void deleteUsersRoles(long userId){
    	delete(getNameSpace() + ".deleteUsersRoles", userId);
    }

}
