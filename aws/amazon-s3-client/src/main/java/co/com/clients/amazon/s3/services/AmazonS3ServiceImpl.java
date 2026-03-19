package co.com.clients.amazon.s3.services;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;

import org.springframework.stereotype.Service;

import co.com.clients.parent.exception.BusinessException;
import co.com.clients.parent.exception.ErrorMessage;
import co.com.clients.parent.exception.ErrorType;
import co.com.clients.parent.exception.IntegrationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import static co.com.clients.amazon.s3.utilities.Constants.AMAZON_S3;
import static co.com.clients.amazon.s3.utilities.Constants.ERROR_WITH_PREFIX;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmazonS3ServiceImpl implements AmazonS3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Override
    public InputStream getObject(String bucketName, String key)
            throws IntegrationException {
        try {
            log.debug("Inicio consulta Bucket output: {}", bucketName);
            log.debug("Prefijo de busqueda generado: {}", key);

            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(key)
                    .build();

            log.debug("Ejecutando listado de objetos S3 con prefijo: {}", key);
            ListObjectsV2Response objectResponse = s3Client.listObjectsV2(request);
            List<S3Object> objects = objectResponse.contents();
            log.debug("Objetos encontrados con el prefijo: {}", objects.size());

            S3Object obj = objects.stream()
                    .findFirst()
                    .orElseThrow(() -> new BusinessException.ValueNotFoundException(ERROR_WITH_PREFIX + key));

            log.debug("Objeto S3 seleccionado: {}", obj.key());
            GetObjectRequest objectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(obj.key())
                    .build();
            log.debug("Obteniendo contenido del objeto S3: {}", obj.key());
            return s3Client.getObject(objectRequest);

        } catch (Exception e) {
            log.error("Error al obtener el objeto S3 del Bucket: " + bucketName + ", key: " + key, e);
            throw new IntegrationException(ErrorType.SYSTEM, ErrorMessage.RESPONSE_SERVICE_ERROR.getValue(), e, AMAZON_S3);
        }
    }

    @Override
    public String generatePresignedUrl(String bucketName, String key, Duration duration) throws IntegrationException {
        try {
            log.debug("Inicio generaciÃ³n URL prefirmada. Bucket: {}, Prefijo: {}, DuraciÃ³n: {}", bucketName, key, duration);

            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(key)
                    .build();

            log.debug("Buscando objeto con prefijo: {}", key);
            ListObjectsV2Response objectResponse = s3Client.listObjectsV2(request);
            List<S3Object> objects = objectResponse.contents();

            S3Object obj = objects.stream()
                    .findFirst()
                    .orElseThrow(() -> new BusinessException.ValueNotFoundException(ERROR_WITH_PREFIX + key));

            String objectKey = obj.key();
            log.debug("Objeto S3 seleccionado para URL prefirmada: {}", objectKey);

            GetObjectRequest objectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(duration)
                    .getObjectRequest(objectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            String url = presignedRequest.url().toString();

            log.debug("URL prefirmada generada exitosamente: {}", url);
            return url;

        } catch (Exception e) {
            log.error("Error al generar URL prefirmada para Bucket: " + bucketName + ", key: " + key, e);
            throw new IntegrationException(ErrorType.SYSTEM, ErrorMessage.RESPONSE_SERVICE_ERROR.getValue(), e, AMAZON_S3);
        }
    }

    @Override
    public void putObject(String bucketName, String key, byte[] content, String contentType) throws IntegrationException {
        try {
            log.debug("Subiendo objeto a S3. Bucket: {}, Key: {}", bucketName, key);
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(content));
            log.debug("Objeto subido exitosamente a S3.");

        } catch (Exception e) {
            log.error("Error al subir objeto a S3. Bucket: " + bucketName + ", key: " + key, e);
            throw new IntegrationException(ErrorType.SYSTEM, ErrorMessage.RESPONSE_SERVICE_ERROR.getValue(), e, AMAZON_S3);
        }
    }
}
