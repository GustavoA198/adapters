package co.com.clients.parent.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

/**
 * @author Zathura Code Generator Version 24.05 http://zathuracode.org/
 * @generationDate 2024-08-05T08:45:56.708048
*/
@Configuration
public class LocaleConfig implements WebMvcConfigurer {

    @Bean
    AcceptHeaderLocaleResolver localeResolver() {
        final AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.of("es"));

        List<Locale> supportedLocales = new ArrayList<>(0);
        supportedLocales.add(Locale.of("es")); // Idioma EspaÃ±ol
//        supportedLocales.add(Locale.US); // Idioma InglÃ©s (Estados Unidos)
        resolver.setSupportedLocales(supportedLocales);
        return resolver;
      }

    @Bean
    ResourceBundleMessageSource messageSource() {
      final ResourceBundleMessageSource source = new ResourceBundleMessageSource();
      source.setDefaultEncoding("UTF-8");
      source.setBasename("messages");
      return source;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LocaleInterceptor());
    }
}
