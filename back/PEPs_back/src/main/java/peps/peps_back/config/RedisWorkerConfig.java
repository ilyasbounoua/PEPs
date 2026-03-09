package peps.peps_back.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;

import java.time.Duration;

/**
 * Specific configuration for the containers that process audio.
 * This class is ONLY activated if the "audio-worker" profile is active on the
 * server.
 * This prevents the main web backend from accidentally consuming audio tasks.
 * 
 * @author Santiago Alexander RODRIGUEZ TRIANA
 */
@Configuration
@Profile("audio-worker")
public class RedisWorkerConfig {

    /**
     * Container that manages the threads listening to Redis Streams.
     * Instead of manually checking the database continuously (hard polling),
     * this container maintains an active subscription and triggers the Workers
     * when new messages arrive.
     * 
     * @param connectionFactory The Redis connection factory injected by Spring
     * @return The configured StreamMessageListenerContainer
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer(
            RedisConnectionFactory connectionFactory) {

        // Configure the container to check for new messages every 1 second (Poll
        // Timeout)
        StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options = StreamMessageListenerContainerOptions
                .builder()
                .pollTimeout(Duration.ofSeconds(1))
                .build();

        return StreamMessageListenerContainer.create(connectionFactory, options);
    }
}
