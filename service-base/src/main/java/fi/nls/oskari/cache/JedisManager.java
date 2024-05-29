package fi.nls.oskari.cache;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.PropertyUtil;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Collections;
import java.util.Set;

/**
 * Manages Jedis connections using JedisPool (connection pool)
 */
public class JedisManager {

    public static final String CLUSTERED_ENV_PROFILE = "redis-session";
    public static String ERROR_REDIS_COMMUNICATION_FAILURE = "redis_communication_failure";
    public static String PUBSUB_CHANNEL_PREFIX = "oskari_";
    public static final int EXPIRY_TIME_DAY = 86400;
    private static final int REDIS_DEFAULT_TIMEOUT_MS = 2000;

    private final static Logger log = LogFactory.getLogger(JedisManager.class);
    private static final JedisManager instance = new JedisManager();
    private static volatile JedisPool pool;

    private static final String KEY_REDIS_HOSTNAME = "redis.hostname";
    private static final String KEY_REDIS_PORT = "redis.port";
    private static final String KEY_REDIS_POOL_SIZE = "redis.pool.size";
    private static Boolean isClustered = null;

    /**
     * Blocking construction of instances from other classes by making constructor private
     */
    private JedisManager() {}

    public static HostAndPort getHostAndPort() {
        return new HostAndPort(JedisManager.getHost(), JedisManager.getPort());
    }

    public static JedisClientConfig getClientConfig() {
        return DefaultJedisClientConfig.builder()
                .connectionTimeoutMillis(getConnectionTimeoutMs())
                .user(getUser())
                .password(getPassword())
                .ssl(getUseSSL())
                .build();
    }

    public static String getHost() {
        return PropertyUtil.get(KEY_REDIS_HOSTNAME, "localhost");
    }
    public static int getPort() {
        return ConversionHelper.getInt(PropertyUtil.get(KEY_REDIS_PORT), 6379);
    }
    public static int getPoolSize() {
        return ConversionHelper.getInt(PropertyUtil.get(KEY_REDIS_POOL_SIZE), 30);
    }
    private static int getConnectionTimeoutMs() {
        return PropertyUtil.getOptional("redis.timeout.connect", REDIS_DEFAULT_TIMEOUT_MS);
    }
    private static String getPassword() {
        return PropertyUtil.get("redis.password", null);
    }
    private static String getUser() {
        return PropertyUtil.get("redis.user", null);
    }
    private static boolean getUseSSL() {
        return PropertyUtil.getOptional("redis.ssl", false);
    }
    private static boolean getBlockWhenExhausted() {
        return PropertyUtil.getOptional("redis.blockExhausted", false);
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
        poolConfig.setMaxTotal(poolSize);
        poolConfig.setTimeBetweenEvictionRunsMillis(-1);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setBlockWhenExhausted(getBlockWhenExhausted());
        final JedisPool oldPool = pool;
        pool = new JedisPool(poolConfig, host, port, getConnectionTimeoutMs(), getUser(), getPassword(), getUseSSL());
        // Should we use the long format to have an option to pass "client name" to Redis to help debugging issues with shared Redis instances?
        // pool = new JedisPool(poolConfig, host, port, getConnectionTimeoutMs(), getSocketReadTimeoutMs(), getPassword(), Protocol.DEFAULT_DATABASE, getClientName());

/*
        // after Jedis 4.0 we could use JedisPooled that shares an interface with JedisCluster and use that as the common "pool" variable
        JedisPooled jedis = new JedisPooled(host, port);

        // For clusters we should use JedisCluster instead (easier after 4.x upgrade, but current Spring version doesn't support it yet)
        Set<HostAndPort> jedisClusterNodes = new HashSet<>();
        jedisClusterNodes.add(new HostAndPort(host, port));
        // jedisClusterNodes.add(new HostAndPort(host, port));
        JedisCluster jedis = new JedisCluster(jedisClusterNodes, poolConfig);
*/
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
        try (Jedis jedis = instance.getJedis(throwException)) {
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
        try (Jedis jedis = instance.getJedis()) {
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

        try (Jedis jedis = instance.getJedis()) {
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
        try (Jedis jedis = instance.getJedis()) {
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
        try (Jedis jedis = instance.getJedis()) {
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
        try (Jedis jedis = instance.getJedis()) {
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
        try (Jedis jedis = instance.getJedis()) {
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
     * @param value
     * @return Long or null when there was an exception
     */
    public static Long hset(String key, String field, String value) {
        try (Jedis jedis = instance.getJedis()) {
            if (jedis == null) {
                return null;
            }
            return jedis.hset(key, field, value);
        } catch(JedisConnectionException e) {
            log.error("Failed to hset", key);
            return null;
        } catch (Exception e) {
            log.error("Setting", key, "failed miserably");
            return null;
        }
    }

    /**
     * Thread-safe Long HDEL for Redis
     *
     * @param key
     * @param fields
     * @return Long or null when there was an exception
     */
    public static Long hdel(String key, String... fields) {
        try (Jedis jedis = instance.getJedis()) {
            if (jedis == null) {
                return null;
            }
            return jedis.hdel(key, fields);
        } catch(JedisConnectionException e) {
            log.error("Failed to hdel", key);
            return null;
        } catch (Exception e) {
            log.error("Removing", key, "with fields:", fields, "failed miserably");
            return null;
        }
    }

    /**
     * Thread-safe Long HINCRBY for Redis
     *
     * @param key
     * @param field
     * @return Long or null when there was an exception
     */
    public static Long hincrBy(String key, String field, long increment) {
        try (Jedis jedis = instance.getJedis()) {
            if (jedis == null) {
                return null;
            }
            return jedis.hincrBy(key, field, increment);
        } catch(JedisConnectionException e) {
            log.error("Failed to hincrBy", key, field);
            return null;
        } catch (Exception e) {
            log.error("Incrementing", key, field, "failed miserably");
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
        try (Jedis jedis = instance.getJedis()) {
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
        try (Jedis jedis = instance.getJedis()) {
            if (jedis == null) {
                return null;
            }
            log.debug("Sending to", PUBSUB_CHANNEL_PREFIX + channel, "msg:", message);
            return jedis.publish(PUBSUB_CHANNEL_PREFIX + channel, message);
        } catch(JedisConnectionException e) {
            log.error("Failed to publish on:", channel);
            return null;
        } catch (Exception e) {
            log.error("Publishing on:", channel, "failed miserably");
            return null;
        }
    }

    public static boolean isClusterEnv() {
        if (isClustered == null) {
            final String[] configuredProfiles = PropertyUtil.getCommaSeparatedList("oskari.profiles");
            isClustered = hasClusterProfile(configuredProfiles);
        }
        return isClustered;
    }

    protected static boolean hasClusterProfile(String[] configuredProfiles) {
        if (configuredProfiles == null) {
            return false;
        }
        for (String profile: configuredProfiles) {
            if (CLUSTERED_ENV_PROFILE.equalsIgnoreCase(profile.trim())) {
                return true;
            }
        }
        return false;
    }
}
