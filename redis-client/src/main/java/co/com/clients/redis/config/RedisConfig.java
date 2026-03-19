package co.com.clients.redis.config;

import java.time.Duration;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Configuration
public class RedisConfig {

	@Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.user}")
    private String user;
    
    @Value("${spring.redis.ttl}")
    private long ttl;
    
    @Value("${spring.redis.password}")
    private String password;

    @Bean
    RedisConnectionFactory redisConnectionFactory() {

        RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(Arrays.asList(host));

            if (password != null && !password.isEmpty()) {
                clusterConfig.setPassword(RedisPassword.of(password));
            }

            return new LettuceConnectionFactory(clusterConfig);

//    	if(host.contains("localhost")) {
//    		RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration(host, port);
//    		if(user != null && !user.isEmpty()) {
//    			serverConfig.setUsername(user);
//    		}
//    		if (password != null && !password.isEmpty()) {
//    			serverConfig.setPassword(password);
//    		}
//
//
//    		return new LettuceConnectionFactory(serverConfig);
//    	}else {
//    		RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(Arrays.asList(host));
//
//            if (password != null && !password.isEmpty()) {
//                clusterConfig.setPassword(RedisPassword.of(password));
//            }
//            
//            return new LettuceConnectionFactory(clusterConfig);
//    	}
    }
    
    @Bean
    RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        // Serializador para las claves
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
//
//        // Serializador para los valores
        
        SafeGenericJackson2JsonRedisSerializer safeSerializer = new SafeGenericJackson2JsonRedisSerializer(redisMapper());
        
        redisTemplate.setValueSerializer(safeSerializer);
        redisTemplate.setHashValueSerializer(safeSerializer);

        redisTemplate.afterPropertiesSet();
        
        return redisTemplate;
    }
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(ttl)) // TTL predeterminado
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
        	.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(redisMapper())));

        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(cacheConfig)
            .build();
    }
    
    private ObjectMapper redisMapper() {
    	ObjectMapper mapper = new ObjectMapper();
    	mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
    	mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    	mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    	
    	mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    	
    	// Permite que Jackson almacene información de tipo en la caché
        mapper.activateDefaultTyping(
            mapper.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL
        );
    	
        return mapper;
    }

    @Bean
    public SimpleKeyGenerator keyGenerator() {
        return new SimpleKeyGenerator(); // Generador de claves predeterminadas
    }
    
    
    public class SafeGenericJackson2JsonRedisSerializer extends GenericJackson2JsonRedisSerializer {

        public SafeGenericJackson2JsonRedisSerializer(ObjectMapper objectMapper) {
            super(objectMapper);
        }

        @Override
        public Object deserialize(byte[] bytes) throws SerializationException {
            try {
                return super.deserialize(bytes);
            } catch (SerializationException e) {
                try {
                    // Si la clase no existe, devolver un Map genérico
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(bytes, Object.class);
                } catch (Exception inner) {
                    throw new SerializationException("Error deserializing as Object fallback", inner);
                }
            } catch (Exception e) {
                throw new SerializationException("Error deserializing Redis value", e);
            }
        }
    }
	
}
