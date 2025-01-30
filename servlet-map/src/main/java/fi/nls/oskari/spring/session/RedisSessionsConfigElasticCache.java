package fi.nls.oskari.spring.session;

import fi.nls.oskari.cache.JedisManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Profile(RedisSessionsConfigElasticCache.PROFILE)
public class RedisSessionsConfigElasticCache implements WebMvcConfigurer {
    public static final String PROFILE = "redis-aws";

    @Bean
    @Profile(JedisManager.CLUSTERED_ENV_PROFILE)
    ConfigureRedisAction configureRedisAction() {
        return ConfigureRedisAction.NO_OP;
    }
}
