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
 * Second link in the Assembly Line (Pipeline 1: Upload Audio to MinIO).
 * This Worker receives the audio that was already processed and validated
 * by the RawAudioWorker, and focuses exclusively on establishing the
 * connection with MinIO to save the file.
 * 
 * @author Santiago Alexander RODRIGUEZ TRIANA
 */
@Component
@Profile("audio-worker")
public class StorageWorker implements StreamListener<String, MapRecord<String, String, String>> {

    @Autowired
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final String streamKey = "storage-upload-stream";
    private final String consumerGroup = "storage-handlers";

    /**
     * Initializes the listener and creates the consumer group if missing.
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
     * Triggered when a message containing processed audio data arrives.
     * 
     * @param message The record retrieved from the Redis Stream containing the
     *                payload.
     */
    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        System.out.println("StorageWorker received job: " + message.getId());

        // TODO: MinIO upload logic.
        // 1. Instantiate MinIO client (Amazon S3 compatible client).
        // 2. Perform putObject(bucket, filename, inputStream).
        System.out.println("StorageWorker simulating upload to MinIO...");

        try {
            // Simulate the network delay of uploading a heavy file to external storage
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // ACK: Confirm the file was successfully uploaded to Storage
        redisTemplate.opsForStream().acknowledge(streamKey, consumerGroup, message.getId());
    }
}
