package facebook.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/// Configuration class for the storage service
@Configuration
public class StorageConfig {

    @Value("${aws.key.access}")
    private String accessKey;

    @Value("${aws.key.secret}")
    private String accessSecret;

    @Value("${aws.region}")
    private String region;

    @Value("${filebase.endpoint}")
    private String filebaseEndpoint;

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, accessSecret);

        return S3Client.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create(filebaseEndpoint))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
}
