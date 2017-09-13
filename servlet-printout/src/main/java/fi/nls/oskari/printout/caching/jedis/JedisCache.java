package fi.nls.oskari.printout.caching.jedis;

import fi.nls.oskari.printout.caching.BlobCache;
import fi.nls.oskari.printout.config.ConfigValue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class JedisCache {

    private static final Log LOG = LogFactory.getLog(JedisCache.class);
    public static final BlobCache CACHE;

    static {
        String host = "localhost";
        int port = 6379;

        String conf = System.getProperty(ConfigValue.CONFIG_SYSTEM_PROPERTY);
        try (InputStream in = conf != null ? new FileInputStream(conf)
                : JedisCache.class.getResourceAsStream(ConfigValue.DEFAULT_PROPERTIES)) {
            Properties props = new Properties();
            props.load(in);
            host = ConfigValue.REDIS_HOST.getConfigProperty(props, host);
            port = ConfigValue.REDIS_PORT.getConfigProperty(props, port);
        } catch (IOException e) {
            LOG.warn(e.getMessage(), e);
        }
        CACHE = new JedisBlobCacheImpl(new JedisPool(new JedisPoolConfig(), host, port));
    }

    private static class JedisBlobCacheImpl implements BlobCache {
        private final JedisPool pool;

        JedisBlobCacheImpl(JedisPool pool) {
            this.pool = pool;
        }

        public byte[] getFromCache(byte[] key) {
            try (Jedis jedis = pool.getResource()) {
                return jedis.get(key);
            }
        }

        public void putToCache(byte[] key, byte[] blob) {
            try (Jedis jedis = pool.getResource()) {
                jedis.set(key, blob);
                jedis.expire(key, 4 * 60 * 60);
            }
        }

    }

}
