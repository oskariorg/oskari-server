package fi.nls.oskari.spring.session;

import fi.nls.oskari.util.PropertyUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

// TODO: Check if maxInactiveIntervalInSeconds can be configured
@Configuration
@Profile(RedisSessionConfig.PROFILE)
@EnableRedisHttpSession(maxInactiveIntervalInSeconds=7200)
public class RedisSessionConfig extends WebMvcConfigurerAdapter {

    public static final String PROFILE ="redis-session";

    @Bean
    public JedisConnectionFactory connectionFactory() {
        JedisConnectionFactory jedis = new JedisConnectionFactory();
        jedis.setHostName(PropertyUtil.get("redis.hostname", "127.0.0.1"));
        jedis.setPort(PropertyUtil.getOptional("redis.port", 6379));
        jedis.setUsePool(true);
        return jedis;
    }
}
