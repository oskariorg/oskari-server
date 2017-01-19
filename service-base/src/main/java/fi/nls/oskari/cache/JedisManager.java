package fi.nls.oskari.cache;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceRuntimeException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Collections;
import java.util.Set;

/**
 * Manages Jedis connections using JedisPool (connection pool)
 */
public class JedisManager {

    private static final JedisManager instance = new JedisManager();

    private static volatile JedisPool pool;

    private final static Logger log = LogFactory.getLogger(JedisManager.class);
    public static String ERROR_REDIS_COMMUNICATION_FAILURE = "redis_communication_failure";

    public static final int EXPIRY_TIME_DAY = 86400;

    /**
     * Blocking construction of instances from other classes by making constructor private
     */
    private JedisManager() {}

    /**
     * Connects configured connection pool to a Redis server
     */
    public static void connect(final int poolSize, final String host, final int port) {
        if(pool != null) {
            log.warn("Pool already created! Connect called multiple times. Tried connecting to:", host);
            return;
        }
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMaxIdle(poolSize / 2);
        poolConfig.setMinIdle(1);
        poolConfig.setTimeBetweenEvictionRunsMillis(-1);
        poolConfig.setTestOnBorrow(true);
        final JedisPool oldPool = pool;
        pool = new JedisPool(poolConfig, host, port);
        log.debug("Created Redis connection pool with host", host, "port", port);
        if (null != oldPool) {
            log.debug("Closing old Jedis pool");
            oldPool.close();
        }
    }

    public static void shutdown() {
        pool.close();
    }

    /**
     * Destroys the pool
     */
    public void release() {
        pool.destroy();
    }



    /**
     * Gets Jedis connection from the pool
     * @return Jedis instance or ServiceRuntimeExceptionin
     */
    public Jedis getJedis() {
        return getJedis(false);
    }

    public Jedis getJedis(boolean throwException) {
        try {
            return pool.getResource();
        } catch (Exception e) {
            log.error("Getting Jedis connection from the pool failed:", e.getMessage());
            if (e.getCause() != null) {
                log.debug(e, "Cause:", e.getCause().getMessage());
            }
            if(throwException) {
                throw new ServiceRuntimeException("Getting Jedis connection from the pool failed: " + e.getMessage(),
                        e.getCause(), ERROR_REDIS_COMMUNICATION_FAILURE);
            }
        }
        return null;
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
	public static String getNecessary(String key) {
	  return get(key, true);
	}

    /**
     * Thread-safe String GET for Redis
     * throws new runtime exception, if any exception found
     * @param key
     * @return string
     */
    public static String get(String key) {
        return get(key, false);
    }
    /**
     * Thread-safe String GET for Redis
     *
     * @param key
     * @param throwException  throws new runtime exception, if any exception found
     * @return string
     */
    public static String get(String key, boolean throwException) {
        Jedis jedis = instance.getJedis(throwException);
        if (jedis == null) return null;

        try {
            return jedis.get(key);
        } catch (JedisConnectionException e) {
            log.error("Failed to get", key, "returning broken connection...");
            pool.returnBrokenResource(jedis);
            log.error("Broken connection closed");
            if (throwException) {
                throw new ServiceRuntimeException("Failed to get " + key + " returning broken connection...: " + e.getMessage(),
                        e.getCause(), ERROR_REDIS_COMMUNICATION_FAILURE);
            }
            return null;
        } catch (Exception e) {
            log.error("Getting", key, "from Redis failed:", e.getMessage());
            if (throwException) {
                throw new ServiceRuntimeException("Getting" + key + "from Redis failed: " + e.getMessage(),
                        e.getCause(), ERROR_REDIS_COMMUNICATION_FAILURE);
            }
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
            log.error("Getting", key, "from Redis failed:", e.getMessage());
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
            log.error("Setting", key, "to Redis failed:", e.getMessage());
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
            log.error("Setting", key, "to Redis failed:", e.getMessage());
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
            log.error("Running KEYS", pattern + "on Redis failed:", e.getMessage());
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
            log.error("Getting HKEYS", key + "on Redis failed:", e.getMessage());
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
            log.error("Getting HGET", key + "on Redis failed:", e.getMessage());
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
     * Returns length of string for a key (0 if key doesn't exist).
     * -1 means system level error.
     * @param key
     * @return
     */
    public static long getValueStringLength(String key) {
        Jedis jedis = instance.getJedis();
        if(jedis == null) {
            return -1;
        }

        try {
            return jedis.strlen(key);
        } catch(JedisConnectionException e) {
            log.error("Failed to strlen", key + "* returning broken connection...");
            pool.returnBrokenResource(jedis);
            log.error("Broken connection closed");
        } catch (Exception e) {
            log.error("Getting key length", key + " failed miserably");
        } finally {
            instance.returnJedis(jedis);
        }
        return -1;
    }
    /**
     * Returns the number of elements inside the list after the push operation.
     * -1 means system level error.
     * @param key
     * @param values
     * @return
     */
    public static long pushToList(String key, String ...values) {
        Jedis jedis = instance.getJedis();
        if(jedis == null) {
            return -1;
        }

        try {
            return jedis.rpush(key, values);
        } catch(JedisConnectionException e) {
            log.error("Failed to rpush", key + "* returning broken connection...");
            pool.returnBrokenResource(jedis);
            log.error("Broken connection closed");
        } catch (Exception e) {
            log.error("Adding to list", key + " failed miserably");
        } finally {
            instance.returnJedis(jedis);
        }
        return -1;
    }

    /**
     * Removes and returns the last element from the list.
     * @param key
     * @return
     */
    public static String popList(String key) {
        return popList(key, false);
    }

    /**
     * Removes and returns an item from list.
     * With head is true uses the first element, with false the last element.
     * @param key the list key
     * @param head
     * @return
     */
    public static String popList(String key, boolean head) {
        Jedis jedis = instance.getJedis();
        if(jedis == null) {
            return null;
        }

        try {
            String value;
            if(head) {
                value =  jedis.lpop(key);
            } else {
                value = jedis.rpop(key);
            }
            if("nil".equalsIgnoreCase(value)) {
                // If the key does not exist or the list is already empty the special value 'nil' is returned.
                return null;
            }
            return value;
        } catch(JedisConnectionException e) {
            log.error("Failed to lpop", key + "* returning broken connection...");
            pool.returnBrokenResource(jedis);
            log.error("Broken connection closed");
        } catch (Exception e) {
            log.error("Popping from list", key + " failed miserably");
        } finally {
            instance.returnJedis(jedis);
        }
        return null;
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
