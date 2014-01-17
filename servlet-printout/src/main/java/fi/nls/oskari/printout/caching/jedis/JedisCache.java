package fi.nls.oskari.printout.caching.jedis;

import fi.nls.oskari.printout.caching.BlobCache;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisCache {

	static private class JedisBlobCacheImpl implements BlobCache {
		JedisPool pool;

		JedisBlobCacheImpl(JedisPool pool) {
			this.pool = pool;
		}

		
		public byte[] getFromCache(byte[] key) {
			byte[] blob = null;
			Jedis jedis = pool.getResource();
			try {
				blob = jedis.get(key);
			} finally {
				pool.returnResource(jedis);
			}
			return blob;
		}

		
		public void putToCache(byte[] key, byte[] blob) {
			Jedis jedis = pool.getResource();
			try {
				jedis.set(key, blob);
				jedis.expire(key, 4 * 60 * 60);
			} finally {
				pool.returnResource(jedis);
			}

		}

	}

	static JedisBlobCacheImpl cache = new JedisBlobCacheImpl(new JedisPool(
			new JedisPoolConfig(), "localhost"));

	public static BlobCache getBlobCache() {
		return cache;
	}

}
