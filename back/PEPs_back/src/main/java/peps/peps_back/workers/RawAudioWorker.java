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
 * First link in the Assembly Line (Pipeline 1: Audio Upload).
 * This "Worker" is subscribed to the raw audio stream.
 * Its function is to receive the audio, process it (compress, validate format),
 * and pass it to the next Stream so it can be saved in storage.
 * 
 * @author Santiago Alexander RODRIGUEZ TRIANA
 */
@Component
@Profile("audio-worker") // Only instantiated if the server copy has the "audio-worker" role
public class RawAudioWorker implements StreamListener<String, MapRecord<String, String, String>> {

    @Autowired
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Name of the "conveyor belt" or Stream setup we will read from
    private final String streamKey = "upload-audio-stream";
    // Name of our worker team (Consumer Group), ensuring tasks are distributed
    private final String consumerGroup = "audio-processors";

    /**
     * This method executes automatically when the server starts.
     * It creates the consumer group and tells the container: "Start listening for
     * new messages here".
     */
    @PostConstruct
    public void init() {
        try {
            // Attempt to create the Consumer Group. If it already exists, throw an
            // exception we ignore.
            redisTemplate.opsForStream().createGroup(streamKey, consumerGroup);
        } catch (Exception e) {
            // Group already exists, which is fine.
        }

        // Start receiving messages (ReadOffset.lastConsumed() asks only for unread
        // messages)
        container.receive(
                Consumer.from(consumerGroup, "worker-1"),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                this);
    }

    /**
     * Main function of the Worker. Triggered sequentially every time a message
     * arrives in the Stream.
     * 
     * @param message The record retrieved from the Redis Stream containing the
     *                payload.
     */
    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        System.out.println("RawAudioWorker received message: " + message.getId());

        // TODO: Actual audio processing logic.
        // Here you would retrieve the file from memory (or a temp DB),
        // verify it's a valid MP3/WAV, maybe normalize the volume, compress it, etc.

        // Simulating that we are "processing" something that takes time
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Once finished, forward the result (validated payload) to the next workstation
        // in the assembly line: the MinIO storage worker.
        redisTemplate.opsForStream().add("storage-upload-stream", message.getValue());
        System.out.println("RawAudioWorker forwarded to storage-upload-stream.");

        // VERY IMPORTANT: Acknowledge (ACK).
        // Confirm to Redis that the task was completed successfully.
        // Without ACK, Redis will assume the worker crashed and will reassign the
        // message.
        redisTemplate.opsForStream().acknowledge(streamKey, consumerGroup, message.getId());
    }
}
