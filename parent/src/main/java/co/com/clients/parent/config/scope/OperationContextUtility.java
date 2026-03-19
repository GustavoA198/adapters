package co.com.clients.parent.config.scope;

import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class OperationContextUtility {

	public void start() {
		String uuid = UUID.randomUUID().toString();
		OperationContextHolder.start(uuid);
	}
}