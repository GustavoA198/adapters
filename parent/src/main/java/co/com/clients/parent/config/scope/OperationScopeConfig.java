package co.com.clients.parent.config.scope;

import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.log4j.Log4j2;

@Configuration
@Log4j2
public class OperationScopeConfig {

    @Bean
    OperationScope operationScope() {
        return new OperationScope();
    }

    @Bean
    CustomScopeConfigurer scopeConfigurer(OperationScope operationScope) {
        CustomScopeConfigurer configurer = new CustomScopeConfigurer();
        configurer.addScope("operation", operationScope);
        return configurer;
    }
    
}