package fi.nls.oskari.cache;

import java.util.Set;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Manages Jedis connections using JedisPool (connection pool)
 */
public class JedisManager {
    private static final JedisManager instance = new JedisManager();
    private static JedisPool pool;

    private final static Logger log = LogFactory.getLogger(JedisManager.class);
    
    /**
     * Blocking construction of instances from other classes by making constructor private
     */
    private JedisManager() {}
    
    /**
     * Returns singleton instance of the manager
     * 
     * @return singleton
     */
    public static final JedisManager getInstance() {
        return instance;
    }

    public static boolean isPoolInitialized() {

        try {
            pool.getResource();
        } catch (Exception e) {
            return false;
        }

        return true;
    }


    /**
     * Connects configured connection pool to a Redis server
     * 
     * @param poolSize
     * @param host
     * @param port
     */
    public static void connect(int poolSize, String host, int port) {
    	
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxActive(poolSize); // pool size
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMaxIdle(poolSize/2);
        poolConfig.setMinIdle(1);
        poolConfig.setNumTestsPerEvictionRun(10);
        poolConfig.setTimeBetweenEvictionRunsMillis(30000);
        
        pool = new JedisPool(poolConfig, host, port);

    }
    
    /**
     * Destroys the pool
     */
    public void release() {
        pool.destroy();
    }
    
    /**
     * Gets Jedis connection from the pool
     * 
     * @return Jedis instance
     */
    public Jedis getJedis() {
        return pool.getResource();
    	//return new Jedis();
    }
    
    /**
     * Returns (releases) Jedis connection back to the pool
     * 
     * @param jedis
     */
    public void returnJedis(Jedis jedis) {
        pool.returnResource(jedis);
    }
    
    /**
     * Thread-safe String GET for Redis
     * 
     * @param key
     * @return string
     */
	public static String get(String key) {
		Jedis jedis = instance.getJedis();
		try {
			return jedis.get(key);
		} catch(Exception e) {
			// catch all fish
            log.error(e, "Failed getting", key, "from Jedis");
			return null;
		} finally {
			if(jedis != null) {
				instance.returnJedis(jedis);
			}
		}
	}
    
    /**
     * Thread-safe byte[] GET for Redis
     * 
     * @param key
     * @return bytes
     */
	public static byte[] get(byte[] key) {
		Jedis jedis = instance.getJedis();
		try {
			return jedis.get(key);
		} finally {
			if(jedis != null) {
				instance.returnJedis(jedis);
			}
		}
	}

    /**
     * Thread-safe String HKEYS for Redis
     * 
     * @param key
     * @return set of string
     */
	public static Set<String> hkeys(String key) {
		Jedis jedis = instance.getJedis();
		try {
			return jedis.hkeys(key);
		} finally {
			if(jedis != null) {
				instance.returnJedis(jedis);
			}
		}
	}
	
    /**
     * Thread-safe String HGET for Redis
     * 
     * @param key
     * @param field
     * @return string
     */
	public static String hget(String key, String field) {
		Jedis jedis = instance.getJedis();
		try {
			return jedis.hget(key, field);
		} finally {
			if(jedis != null) {
				instance.returnJedis(jedis);
			}
		}
	}
}