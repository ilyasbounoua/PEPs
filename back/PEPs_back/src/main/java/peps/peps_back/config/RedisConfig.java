package peps.peps_back.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Main Redis connection configuration.
 * This class establishes the physical connection to the Redis server
 * and creates the "RedisTemplate", which is the primary tool used throughout
 * the application to read and write data to Redis.
 * 
 * @author Santiago Alexander RODRIGUEZ TRIANA
 */
@Configuration
public class RedisConfig {

    @Autowired
    private Environment env;

    /**
     * Defines the connection factory using Lettuce.
     * Reads credentials and connection details from environment variables (.env).
     * 
     * @return LettuceConnectionFactory configured with host, port, and password.
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        String host = env.getProperty("REDIS_HOST", "localhost");
        int port = Integer.parseInt(env.getProperty("REDIS_PORT", "6379"));
        String password = env.getProperty("REDIS_PASSWORD", "");

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        if (!password.isEmpty()) {
            config.setPassword(password);
        }
        return new LettuceConnectionFactory(config);
    }

    /**
     * The primary tool for Spring Data Redis.
     * It handles the serialization (conversion) of Java objects to text (or bytes)
     * so that Redis can store them in memory.
     * 
     * @return RedisTemplate configured with String serializers for human-readable
     *         keys.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());

        // Use StringRedisSerializer so that keys are readable as plain text in the
        // Redis DB
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        return template;
    }
}
