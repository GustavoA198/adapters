package co.com.clients.amazon.s3.services;

import java.io.InputStream;
import java.time.Duration;

import co.com.clients.parent.exception.IntegrationException;

public interface AmazonS3Service {

	InputStream getObject(String bucketName, String key) throws IntegrationException;

	String generatePresignedUrl(String bucketName, String key, Duration duration) throws IntegrationException;

	void putObject(String bucketName, String key, byte[] content, String contentType) throws IntegrationException;
}
