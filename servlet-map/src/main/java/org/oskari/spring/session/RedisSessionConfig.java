package org.oskari.spring.session;

import fi.nls.oskari.cache.JedisManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import redis.clients.jedis.JedisClientConfig;

// TODO: Check if maxInactiveIntervalInSeconds can be configured
@Configuration
@Profile(RedisSessionConfig.PROFILE)
@EnableRedisHttpSession(maxInactiveIntervalInSeconds=7200)
public class RedisSessionConfig implements WebMvcConfigurer {

    public static final String PROFILE = JedisManager.CLUSTERED_ENV_PROFILE;

    @Bean
    public JedisConnectionFactory connectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(
                JedisManager.getHost(), JedisManager.getPort());
        JedisClientConfig clientConfig = JedisManager.getClientConfig();
        config.setUsername(clientConfig.getUser());

        RedisPassword pw = RedisPassword.of(clientConfig.getPassword());
        config.setPassword(pw);
        JedisConnectionFactory jedis = new JedisConnectionFactory(config);
        // for some reason a call to afterPropertiesSet() is required for user/passwd to be used
        jedis.afterPropertiesSet();
        return jedis;
    }
}
