package fi.nls.oskari.cache;

import java.util.Collections;
import java.util.Set;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * Manages Jedis connections using JedisPool (connection pool)
 */
public class JedisManager {
    private static final JedisManager instance = new JedisManager();
    private static JedisPool pool;

    private final static Logger log = LogFactory.getLogger(JedisManager.class);

    public static final int EXPIRY_TIME_DAY = 86400;
    
    /**
     * Blocking construction of instances from other classes by making constructor private
     */
    private JedisManager() {}

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
        try {
            return pool.getResource();
        } catch (Exception e) {
            log.error("Getting Jedis connection from the pool failed");
            return null;
        }
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
        if(jedis == null) return null;

		try {
			return jedis.get(key);
		} catch(JedisConnectionException e) {
            log.error("Failed to get", key, "returning broken connection...");
            pool.returnBrokenResource(jedis);
            log.error("Broken connection closed");
			return null;
        } catch (Exception e) {
            log.error("Getting", key, "failed miserably");
            return null;
		} finally {
			instance.returnJedis(jedis);
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
        if(jedis == null) return null;

        try {
			return jedis.get(key);
        } catch(JedisConnectionException e) {
            log.error("Failed to get", key, "returning broken connection...");
            pool.returnBrokenResource(jedis);
            log.error("Broken connection closed");
            return null;
        } catch (Exception e) {
            log.error("Getting", key, "failed miserably");
            return null;
        } finally {
            instance.returnJedis(jedis);
        }
	}

    /**
     * Thread-safe String SETEX for Redis
     *
     * @param key
     * @param seconds
     * @param value
     * @return string
     */
    public static String setex(String key, int seconds, String value) {
        Jedis jedis = instance.getJedis();
        if(jedis == null) return null;

        try {
            return jedis.setex(key, seconds, value);
        } catch(JedisConnectionException e) {
            log.error("Failed to set", key, "returning broken connection...");
            pool.returnBrokenResource(jedis);
            log.error("Broken connection closed");
            return null;
        } catch (Exception e) {
            log.error("Setting", key, "failed miserably");
            return null;
        } finally {
            instance.returnJedis(jedis);
        }
    }

    /**
     * Thread-safe byte[] SETEX for Redis
     *
     * @param key
     * @param seconds
     * @param value
     * @return string
     */
    public static String setex(byte[] key, int seconds, byte[] value) {
        Jedis jedis = instance.getJedis();
        if(jedis == null) return null;

        try {
            return jedis.setex(key, seconds, value);
        } catch(JedisConnectionException e) {
            log.error("Failed to set", key, "returning broken connection...");
            pool.returnBrokenResource(jedis);
            log.error("Broken connection closed");
            return null;
        } catch (Exception e) {
            log.error("Setting", key, "failed miserably");
            return null;
        } finally {
            instance.returnJedis(jedis);
        }
    }

    /**
     * Thread-safe KEYS
     *
     * @param pattern
     * @return keys
     */
    public static Set<String> keys(String pattern) {
        Jedis jedis = instance.getJedis();
        if(jedis == null) return Collections.emptySet();

        try {
            return jedis.keys(pattern + "*");
        } catch(JedisConnectionException e) {
            log.error("Failed to run KEYS", pattern + " returning broken connection...");
            pool.returnBrokenResource(jedis);
            log.error("Broken connection closed");
            return null;
        } catch (Exception e) {
            log.error("Running KEYS", pattern + " failed miserably");
            return null;
        } finally {
            instance.returnJedis(jedis);
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
        if(jedis == null) return Collections.emptySet();

        try {
			return jedis.hkeys(key);
        } catch(JedisConnectionException e) {
            log.error("Failed to hkeys", key, "returning broken connection...");
            pool.returnBrokenResource(jedis);
            log.error("Broken connection closed");
            return null;
        } catch (Exception e) {
            log.error("Getting", key, "failed miserably");
            return null;
        } finally {
            instance.returnJedis(jedis);
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
        if(jedis == null) return null;

        try {
			return jedis.hget(key, field);
        } catch(JedisConnectionException e) {
            log.error("Failed to hget", key, "returning broken connection...");
            pool.returnBrokenResource(jedis);
            log.error("Broken connection closed");
            return null;
        } catch (Exception e) {
            log.error("Getting", key, "failed miserably");
            return null;
        } finally {
            instance.returnJedis(jedis);
        }
	}

    /**
     * Thread-safe Long HSET for Redis
     *
     * @param key
     * @param field
     * @return string
     */
    public static Long hset(String key, String field, String value) {
        Jedis jedis = instance.getJedis();
        if(jedis == null) return null;

        try {
            return jedis.hset(key, field, value);
        } catch(JedisConnectionException e) {
            log.error("Failed to hget", key, "returning broken connection...");
            pool.returnBrokenResource(jedis);
            log.error("Broken connection closed");
            return null;
        } catch (Exception e) {
            log.error("Getting", key, "failed miserably");
            return null;
        } finally {
            instance.returnJedis(jedis);
        }
    }

    /**
     * Thread-safe Long DEL for Redis
     *
     * @param keys
     * @return long
     */
    public static Long del(String... keys) {
        Jedis jedis = instance.getJedis();
        if(jedis == null) return null;

        try {
            return jedis.del(keys);
        } catch(JedisConnectionException e) {
            log.error("Failed to delete", keys, "returning broken connection...");
            pool.returnBrokenResource(jedis);
            log.error("Broken connection closed");
            return null;
        } catch (Exception e) {
            log.error("Deleting", keys, "failed miserably");
            return null;
        } finally {
            instance.returnJedis(jedis);
        }
    }

    /**
     * Thread-safe Long DEL with key set
     *
     * @param key
     * @return long
     */
    public static Long delAll(String key) {
        Jedis jedis = instance.getJedis();
        if(jedis == null) return null;

        try {
            Set<String> keys = jedis.keys(key + "*");
            if(keys.size() > 0) {
                return jedis.del(keys.toArray(new String[keys.size()]));
            }
            return 0L;
        } catch(JedisConnectionException e) {
            log.error("Failed to del", key + "* returning broken connection...");
            pool.returnBrokenResource(jedis);
            log.error("Broken connection closed");
            return null;
        } catch (Exception e) {
            log.error("Deleting", key + "* failed miserably");
            return null;
        } finally {
            instance.returnJedis(jedis);
        }
    }

    /**
     * Thread-safe PUBLISH
     *
     * @param channel
     * @param message
     * @return long
     */
    public static Long publish(final String channel, final String message) {
        final Jedis jedis = instance.getJedis();
        if(jedis == null) return null;

        try {
            return jedis.publish(channel, message);
        } catch(JedisConnectionException e) {
            log.error("Failed to publish on:", channel, "returning broken connection...");
            pool.returnBrokenResource(jedis);
            log.error("Broken connection closed");
            return null;
        } catch (Exception e) {
            log.error("Publishing on:", channel, "failed miserably");
            return null;
        } finally {
            instance.returnJedis(jedis);
        }
    }

    /**
     * Thread-safe SUBSCRIBE
     *
     * @param subscriber
     * @param channel
     */
    public static void subscribe(final JedisSubscriber subscriber, final String channel) {
        final Jedis jedis = instance.getJedis();
        if(jedis == null) return;

        new Thread(new Runnable() {
            public void run() {
                try {
                    log.warn("Subscribing on", channel);
                    jedis.subscribe(subscriber, channel);
                } catch(JedisConnectionException e) {
                    log.error("Failed to subscribe on:", channel, "returning broken connection...");
                    pool.returnBrokenResource(jedis);
                    log.error("Broken connection closed");
                } catch (Exception e) {
                    log.error("Subscribing on:", channel, "failed miserably");
                } finally {
                    log.warn("Unsubscribing on:", channel);
                    subscriber.unsubscribe();
                    instance.returnJedis(jedis);
                }
            }
        }).start();
    }
}