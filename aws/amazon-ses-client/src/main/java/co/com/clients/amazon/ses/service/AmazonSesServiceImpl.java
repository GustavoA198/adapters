package co.com.clients.amazon.ses.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import co.com.clients.amazon.ses.dto.AmazonSesEmailDTO;
import co.com.clients.parent.exception.ErrorMessage;
import co.com.clients.parent.exception.ErrorType;
import co.com.clients.parent.exception.IntegrationException;
import jakarta.mail.BodyPart;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeUtility;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@Scope("singleton")
public class AmazonSesServiceImpl implements AmazonSesService {

	@Value("${aws.ses.host}")
	private String host;
	
	@Value("${aws.ses.port}")
	private Integer port;
	
	@Value("${aws.ses.accessKey}")
	private String username;
	
	@Value("${aws.ses.secretAccessKey}")
	private String password;

	@Override
	public void sendEmail(AmazonSesEmailDTO request) throws IntegrationException {

		String from = request.getFrom();
		List<String> to = request.getTo();
		String subject = request.getSubject();
		String htmlBody = request.getBody();
		List<File> file = request.getFile();
		
		try {
	        Session session = Session.getDefaultInstance(getProperties());
	        
	        // Crear el mensaje de correo electrÃ³nico
	        MimeMessage mimeMessage = new MimeMessage(session);
	        mimeMessage.setFrom(new InternetAddress(from));
	        mimeMessage.setRecipient(RecipientType.TO, null);
			for (String recipient : to) {
				mimeMessage.addRecipient(RecipientType.TO, new InternetAddress(recipient));
			}
	        mimeMessage.setSubject(subject);

	        // Crear el cuerpo del mensaje
	        /* MimeBodyPart bodyPart = new MimeBodyPart();
	        bodyPart.setContent(htmlBody, "text/html"); */
			BodyPart bodyPart = new MimeBodyPart();
			bodyPart.setHeader("Content-Type", "text/html; charset=\"utf-8\"");
			bodyPart.setContent(htmlBody, "text/html; charset=utf-8");
			bodyPart.setHeader("Content-Transfer-Encoding", "quoted-printable");

	        // Crear el multipart para el mensaje
	        MimeMultipart multipart = new MimeMultipart();
	        multipart.addBodyPart(bodyPart);

	        if (file != null && !file.isEmpty()) {
				for (File f : file) {
					MimeBodyPart attachmentPart = new MimeBodyPart();
					attachmentPart.attachFile(f);
					attachmentPart.setFileName(MimeUtility.encodeText(f.getName()));
					multipart.addBodyPart(attachmentPart);
				}
			}

	        // Configurar el mensaje con el multipart
	        mimeMessage.setContent(multipart);

	        // Convertir el mensaje a bytes
	        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	        mimeMessage.writeTo(outputStream);
	        
	        // Create a transport.
	        Transport transport = session.getTransport();
	        
	        // Connect to Amazon SES using the SMTP username and password you specified above.
	        transport.connect(host, username, password);
            
            // Send the email.
            transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
		} catch (Exception e) {
			log.error("Error al enviar el correo electr\u00f3nico", e);
			throw new IntegrationException(ErrorType.SYSTEM, ErrorMessage.RESPONSE_SERVICE_ERROR.getValue(), e, "sendEmail");
		}
	}
	
	private Properties getProperties() {
		Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", port); 
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        
        return props;
	}
}