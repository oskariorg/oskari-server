package fi.nls.oskari.spring.session;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.util.PropertyUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

// TODO: Check if maxInactiveIntervalInSeconds can be configured
@Configuration
@Profile(RedisSessionConfig.PROFILE)
@EnableRedisHttpSession(maxInactiveIntervalInSeconds=7200)
public class RedisSessionConfig extends WebMvcConfigurerAdapter {

    public static final String PROFILE = JedisManager.CLUSTERED_ENV_PROFILE;

    @Bean
    public JedisConnectionFactory connectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(
                JedisManager.getHost(), JedisManager.getPort());
        JedisConnectionFactory jedis = new JedisConnectionFactory(config);
        return jedis;
    }
}
