package co.com.clients.redis.service;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Scope("singleton")
public class RedisServiceImpl implements RedisService {
	
	@Value("${spring.redis.ttl}")
	private long ttl;

	private final RedisTemplate<String, Object> redisTemplate;

    public RedisServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
	
	// Almacena un valor en la caché con un TTL dinámico
    @Override
    public void saveToCache(String key, Object value, Long ttl) {
    	if(ttl != null && ttl.equals(-1L)) {
    		redisTemplate.opsForValue().set(key, value);
    	}else {
    		redisTemplate.opsForValue().set(key, value, ttl != null ? ttl : ttl, TimeUnit.MINUTES);
    	}
    }

    // Recupera un valor de la caché
    @Override
    public Object getFromCache(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // Verifica si una clave existe
    @Override
    public boolean existsInCache(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // Elimina un valor de la caché
    @Override
    public void deleteFromCache(String key) {
        redisTemplate.delete(key);
    }
    
    // Obtiene el tiempo de vida restante de una clave
    @Override
    public Long getTTL(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }
	
}