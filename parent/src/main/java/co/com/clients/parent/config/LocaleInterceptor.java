package co.com.clients.parent.config;

import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * @author Zathura Code Generator Version 24.05 http://zathuracode.org/
 * @generationDate 2024-08-05T08:45:56.708048
*/
@Component
@RequiredArgsConstructor
public class LocaleInterceptor implements HandlerInterceptor {
	
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String acceptLanguage = request.getHeader("Accept-Language");

        if (acceptLanguage != null && !acceptLanguage.isEmpty()) {
            Locale locale = Locale.forLanguageTag(acceptLanguage);
            LocaleContextHolder.setLocale(locale);
        }else {
        	Locale locale = Locale.forLanguageTag("es");
            LocaleContextHolder.setLocale(locale);
        }
        
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        LocaleContextHolder.resetLocaleContext();
    }
}
