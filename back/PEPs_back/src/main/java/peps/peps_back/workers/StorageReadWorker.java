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
 * First link in the Download Chain (Pipeline 2: Request from ESP32).
 * This Worker receives the instruction (the file ID) from the web controller,
 * and handles talking to MinIO to download the raw audio bytes.
 * 
 * @author Santiago Alexander RODRIGUEZ TRIANA
 */
@Component
@Profile("audio-worker")
public class StorageReadWorker implements StreamListener<String, MapRecord<String, String, String>> {

    @Autowired
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final String streamKey = "retrieval-request-stream";
    private final String consumerGroup = "storage-readers";

    /**
     * Connects this worker to the incoming request stream.
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
     * Executed when a download request is triggered by an HTTP controller.
     * 
     * @param message Contains the requested 'fileId'.
     */
    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        System.out.println("StorageReadWorker received download request: " + message.getId());

        // TODO: Call MinIO.
        // Using the "fileId" provided inside message.getValue(), connect to the S3
        // Bucket
        // and download the byte array (.wav / .mp3).
        System.out.println("StorageReadWorker fetching from MinIO...");

        // Delegate the downloaded file to the next block (Formatting pipeline for
        // ESP32)
        redisTemplate.opsForStream().add("device-delivery-stream", message.getValue());
        System.out.println("StorageReadWorker forwarded to device-delivery-stream.");

        // ACK: Confirm that reading from Storage was successful
        redisTemplate.opsForStream().acknowledge(streamKey, consumerGroup, message.getId());
    }
}
