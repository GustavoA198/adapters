package co.com.clients.amazon.ses.service;

import co.com.clients.amazon.ses.dto.AmazonSesEmailDTO;
import co.com.clients.parent.exception.IntegrationException;

public interface AmazonSesService {

	public void sendEmail(AmazonSesEmailDTO request) throws IntegrationException;
	
}
