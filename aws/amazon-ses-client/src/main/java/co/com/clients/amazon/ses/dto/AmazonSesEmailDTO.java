package co.com.clients.amazon.ses.dto;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class AmazonSesEmailDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private String from;
	private List<String> to;
	private String subject;
	private String body;
	private List<File> file;
}
