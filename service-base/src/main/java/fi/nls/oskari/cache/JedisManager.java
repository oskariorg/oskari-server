package fi.nls.oskari.cache;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.PropertyUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Manages Jedis connections using JedisPool (connection pool)
 */
public class JedisManager {

    public static String ERROR_REDIS_COMMUNICATION_FAILURE = "redis_communication_failure";
    public static final int EXPIRY_TIME_DAY = 86400;

    private final static Logger log = LogFactory.getLogger(JedisManager.class);
    private static final JedisManager instance = new JedisManager();
    private static volatile JedisPool pool;

    private static final String KEY_REDIS_HOSTNAME = "redis.hostname";
    private static final String KEY_REDIS_PORT = "redis.port";
    private static final String KEY_REDIS_POOL_SIZE = "redis.pool.size";


    /**
     * Blocking construction of instances from other classes by making constructor private
     */
    private JedisManager() {}

    public static String getHost() {
        return PropertyUtil.get(KEY_REDIS_HOSTNAME, "localhost");
    }
    public static int getPort() {
        return ConversionHelper.getInt(PropertyUtil.get(KEY_REDIS_PORT), 6379);
    }
    public static int getPoolSize() {
        return ConversionHelper.getInt(PropertyUtil.get(KEY_REDIS_POOL_SIZE), 30);
    }

    public static void connect() {
        JedisManager.connect(getPoolSize(), getHost(), getPort());
    }
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
        try (Jedis jedis = instance.getJedis(throwException)){
            if (jedis == null) {
                return null;
            }
            return jedis.get(key);
        } catch (JedisConnectionException e) {
            log.error("Failed to get", key);
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
        }
    }

    /**
     * Thread-safe byte[] GET for Redis
     *
     * @param key
     * @return bytes
     */
	public static byte[] get(byte[] key) {
        try (Jedis jedis = instance.getJedis()){
            if (jedis == null) {
                return null;
            }
			return jedis.get(key);
        } catch(JedisConnectionException e) {
            log.error("Failed to get", key);
            log.error("Broken connection closed");
            return null;
        } catch (Exception e) {
            log.error("Getting", key, "from Redis failed:", e.getMessage());
            return null;
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

        try (Jedis jedis = instance.getJedis()){
            if (jedis == null) {
                return null;
            }
            return jedis.setex(key, seconds, value);
        } catch(JedisConnectionException e) {
            log.error("Failed to set", key);
            return null;
        } catch (Exception e) {
            log.error("Setting", key, "to Redis failed:", e.getMessage());
            return null;
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
        try (Jedis jedis = instance.getJedis()){
            if (jedis == null) {
                return null;
            }
            return jedis.setex(key, seconds, value);
        } catch(JedisConnectionException e) {
            log.error("Failed to set", key);
            return null;
        } catch (Exception e) {
            log.error("Setting", key, "to Redis failed:", e.getMessage());
            return null;
        }
    }

    /**
     * Thread-safe KEYS
     *
     * @param pattern
     * @return keys
     */
    public static Set<String> keys(String pattern) {
        try (Jedis jedis = instance.getJedis()){
            if (jedis == null) {
                return Collections.emptySet();
            }
            return jedis.keys(pattern + "*");
        } catch(JedisConnectionException e) {
            log.error("Failed to run KEYS", pattern);
            return null;
        } catch (Exception e) {
            log.error("Running KEYS", pattern + "on Redis failed:", e.getMessage());
            return null;
        }
    }

    /**
     * Thread-safe String HKEYS for Redis
     *
     * @param key
     * @return set of string
     */
	public static Set<String> hkeys(String key) {
        try (Jedis jedis = instance.getJedis()){
            if (jedis == null) {
                return Collections.emptySet();
            }
			return jedis.hkeys(key);
        } catch(JedisConnectionException e) {
            log.error("Failed to hkeys", key);
            return null;
        } catch (Exception e) {
            log.error("Getting HKEYS", key + "on Redis failed:", e.getMessage());
            return null;
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
        try (Jedis jedis = instance.getJedis()){
            if (jedis == null) {
                return null;
            }
			return jedis.hget(key, field);
        } catch(JedisConnectionException e) {
            log.error("Failed to hget", key);
            return null;
        } catch (Exception e) {
            log.error("Getting HGET", key + "on Redis failed:", e.getMessage());
            return null;
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
        try (Jedis jedis = instance.getJedis()){
            if (jedis == null) {
                return null;
            }
            return jedis.hset(key, field, value);
        } catch(JedisConnectionException e) {
            log.error("Failed to hget", key);
            return null;
        } catch (Exception e) {
            log.error("Getting", key, "failed miserably");
            return null;
        }
    }

    /**
     * Thread-safe Long DEL for Redis
     *
     * @param keys
     * @return long
     */
    public static Long del(String... keys) {
        try (Jedis jedis = instance.getJedis()){
            if (jedis == null) {
                return null;
            }
            return jedis.del(keys);
        } catch(JedisConnectionException e) {
            log.error("Failed to delete", keys);
            return null;
        } catch (Exception e) {
            log.error("Deleting", keys, "failed miserably");
            return null;
        }
    }

    /**
     * Thread-safe Long DEL with key set
     *
     * @param key
     * @return long
     */
    public static Long delAll(String key) {
        try (Jedis jedis = instance.getJedis()){
            if (jedis == null) {
                return null;
            }
            Set<String> keys = jedis.keys(key + "*");
            if(keys.size() > 0) {
                return jedis.del(keys.toArray(new String[keys.size()]));
            }
            return 0L;
        } catch(JedisConnectionException e) {
            log.error("Failed to del", key + "*");
            return null;
        } catch (Exception e) {
            log.error("Deleting", key + "* failed miserably");
            return null;
        }
    }

    /**
     * Returns length of string for a key (0 if key doesn't exist).
     * -1 means system level error.
     * @param key
     * @return
     */
    public static long getValueStringLength(String key) {
        try (Jedis jedis = instance.getJedis()){
            if (jedis == null) {
                return -1;
            }
            return jedis.strlen(key);
        } catch(JedisConnectionException e) {
            log.error("Failed to strlen", key);
        } catch (Exception e) {
            log.error("Getting key length", key + " failed miserably");
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
        try (Jedis jedis = instance.getJedis()){
            if (jedis == null) {
                return -1;
            }
            return jedis.rpush(key, values);
        } catch(JedisConnectionException e) {
            log.error("Failed to rpush", key);
        } catch (Exception e) {
            log.error("Adding to list", key + " failed miserably");
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

        try (Jedis jedis = instance.getJedis()){
            if (jedis == null) {
                return null;
            }
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
            log.error("Failed to lpop", key);
        } catch (Exception e) {
            log.error("Popping from list", key + " failed miserably");
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
        try (Jedis jedis = instance.getJedis()){
            if (jedis == null) {
                return null;
            }
            return jedis.publish(channel, message);
        } catch(JedisConnectionException e) {
            log.error("Failed to publish on:", channel);
            return null;
        } catch (Exception e) {
            log.error("Publishing on:", channel, "failed miserably");
            return null;
        }
    }

    /**
     * Thread-safe SUBSCRIBE
     * @deprecated Use org.oskari.cache.JedisListener instead
     *
     * @param subscriber
     * @param channel
     */
    @Deprecated
    public static void subscribe(final JedisSubscriber subscriber, final String channel) {
        new Thread(() -> {
            // "Make sure the subscriber and publisher threads do not share the same Jedis connection."
            // A client subscribed to one or more channels should not issue commands,
            // although it can subscribe and unsubscribe to and from other channels.
            // NOTE!! create a new client for subscriptions instead of using pool to make sure clients don't conflict
                try (Jedis jedis = new Jedis(getHost(), getPort())) {
                    if (jedis == null) {
                        return;
                    }
                    log.warn("Subscribing on", channel);
                    // Subscribe is a blocking action hence the thread
                    // Also we don't care about pooling here since
                    // the client remains blocked for subscription
                    jedis.subscribe(subscriber, channel);
                } catch (Exception e) {
                    log.error(e,"Subscribing on:", channel, "failed");
                }
            }
        ).start();
    }

}
