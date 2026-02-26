package peps.peps_back.workers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Second link in the Download Chain (Pipeline 2: From MinIO to ESP32).
 * This Worker takes the raw audio that has been downloaded from MinIO and
 * formats it (transcoding, changing sample rate in Hz) specifically so that
 * the ESP32 can reproduce it on its DAC without crashing.
 * 
 * @author Santiago Alexander RODRIGUEZ TRIANA
 */
@Component
@Profile("audio-worker")
public class DevicePrepWorker implements StreamListener<String, MapRecord<String, String, String>> {

    @Autowired
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final String streamKey = "device-delivery-stream";
    private final String consumerGroup = "device-formatters";

    /**
     * Binds to the delivery stream to wait for raw audio chunks.
     */
    @PostConstruct
    public void init() {
        try {
            redisTemplate.opsForStream().createGroup(streamKey, consumerGroup);
        } catch (Exception e) {
            // Group already exists
        }
        container.receive(
                Consumer.from(consumerGroup, "worker-1"),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                this);
    }

    /**
     * Triggered once StorageReadWorker finishes fetching the file.
     * 
     * @param message Message containing a reference to the bytes.
     */
    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        System.out.println("DevicePrepWorker preparing audio for ESP32: " + message.getId());

        // TODO: Transcoding logic.
        // E.g., Transform 44100Hz Stereo to 16000Hz Mono so the ESP32 amplifier can
        // handle it.
        System.out.println("DevicePrepWorker formatting audio...");

        // TODO: Fast Delivery Cache.
        // Save the resulting byte block under a Redis Key (E.g., "ready-audio:12345")
        // with a short TTL (Time To Live) of 5 minutes, so the Endpoint can pick it up
        // and return it via HTTP.

        // ACK: Confirm that the ESP32 now has its processed audio ready.
        redisTemplate.opsForStream().acknowledge(streamKey, consumerGroup, message.getId());
    }
}
