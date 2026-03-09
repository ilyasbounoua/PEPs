package peps.peps_back.services;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SetBucketPolicyArgs;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service layer for sound file persistence in MinIO.
 *
 * <p>This service encapsulates bucket provisioning and object lifecycle operations used by
 * controllers.
 *
 * @author Haytam BEN SRIBIT
 */
@Service
public class MinioStorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MinioStorageService.class);
    private static final Pattern SAFE_NAME = Pattern.compile("[^a-zA-Z0-9\\s]");

    private final MinioClient minioClient;
    private final String bucketName;

    /**
     * Builds a MinIO storage service with connection settings read from environment-backed
     * properties.
     *
     * @param endpoint MinIO server endpoint
     * @param accessKey MinIO access key
     * @param secretKey MinIO secret key
     * @param bucketName target bucket used for sound objects
     */
    public MinioStorageService(
            @Value("${MINIO_ENDPOINT:http://localhost:9000}") String endpoint,
            @Value("${MINIO_ACCESS_KEY:minioadmin}") String accessKey,
            @Value("${MINIO_SECRET_KEY:minioadmin}") String secretKey,
            @Value("${MINIO_BUCKET:peps-sounds}") String bucketName) {
        this.bucketName = bucketName.trim();

        this.minioClient = MinioClient.builder()
                .endpoint(endpoint.trim())
                .credentials(accessKey.trim(), secretKey.trim())
                .build();
    }

    /**
     * Ensures the target bucket exists and enforces a private bucket policy at startup.
     */
    @PostConstruct
    public void initializeBucket() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            // Keep bucket private; files are served through backend authorization.
            String privatePolicy = "{\"Version\":\"2012-10-17\",\"Statement\":[]}";
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(bucketName)
                    .config(privatePolicy)
                    .build());
        } catch (Exception e) {
            LOGGER.warn("MinIO initialization skipped: {}", e.getMessage());
        }
    }

    /**
     * Uploads a sound stream and returns the generated object key.
     *
     * @param name sound name used in key generation
     * @param type sound type used as key prefix
     * @param extension source extension
     * @param contentType object content type
     * @param inputStream stream carrying file data
     * @param size stream length in bytes
     * @return generated MinIO object key
     * @throws Exception when bucket or upload operations fail
     */
    public String uploadSound(String name, String type, String extension, String contentType, InputStream inputStream,
            long size) throws Exception {
        ensureBucketExists();
        String objectKey = buildObjectKey(name, type, extension);
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectKey)
                .stream(inputStream, size, -1)
                .contentType(contentType)
                .build());
        return objectKey;
    }

    /**
     * Downloads a sound object as a byte array.
     *
     * @param objectKey object key in MinIO
     * @return object binary content
     * @throws Exception when object retrieval fails
     */
    public byte[] downloadSound(String objectKey) throws Exception {
        ensureBucketExists();
        try (InputStream in = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucketName).object(objectKey).build());
                ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] data = new byte[8192];
            int nRead;
            while ((nRead = in.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            return buffer.toByteArray();
        }
    }

    /**
     * Deletes a sound object from MinIO.
     *
     * @param objectKey object key in MinIO
     * @throws Exception when object removal fails
     */
    public void deleteSound(String objectKey) throws Exception {
        ensureBucketExists();
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectKey)
                .build());
    }

    /**
     * Builds a safe and deterministic object key from logical sound information.
     *
     * @param name sound name
     * @param type sound type/category
     * @param extension file extension
     * @return normalized object key path
     */
    private String buildObjectKey(String name, String type, String extension) {
        String sanitizedName = SAFE_NAME.matcher(name).replaceAll("_").trim().replaceAll("\\s+", "_");
        String safeType = SAFE_NAME.matcher(type).replaceAll("_").trim().replaceAll("\\s+", "_");
        if (safeType.isEmpty()) {
            safeType = "other";
        }
        String safeExtension = extension.replace(".", "").trim();
        if (safeExtension.isEmpty()) {
            safeExtension = "bin";
        }
        if (sanitizedName.isEmpty()) {
            sanitizedName = "sound";
        }
        return safeType + "/" + sanitizedName + "_" + System.currentTimeMillis() + "." + safeExtension.toLowerCase();
    }

    /**
     * Creates the bucket on demand if it does not exist.
     *
     * @throws Exception when MinIO bucket checks or creation fail
     */
    private void ensureBucketExists() throws Exception {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }
}
