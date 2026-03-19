package co.com.clients.parent.service;

import java.util.Locale;

import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
* @author Zathura Code Generator Version 24.05 http://zathuracode.org/
* 
* @generationDate 2024-08-05T08:45:56.708048
*
*/

@Service
@Scope("singleton")
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
	
    private final ResourceBundleMessageSource source;
	
	public String getMessage(String code, String... params) {
		Locale locale = LocaleContextHolder.getLocale();
        
		try {
			String message = source.getMessage(code, params, locale);
			return message;
		} catch (Exception e) {
			return code;
		}
	}
	
}
