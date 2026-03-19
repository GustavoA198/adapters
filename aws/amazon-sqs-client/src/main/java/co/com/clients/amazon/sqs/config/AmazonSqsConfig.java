package co.com.clients.amazon.sqs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
public class AmazonSqsConfig {

	@Value("${aws.sqs.accessKey}")
    private String awsAccessKey;

    @Value("${aws.sqs.secretAccessKey}")
    private String awsSecretAccessKey;

    @Value("${aws.sqs.region}")
    private String region;

    @Bean
    SqsAsyncClient sqsClient() {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(awsAccessKey, awsSecretAccessKey);
        return SqsAsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }
	
}
