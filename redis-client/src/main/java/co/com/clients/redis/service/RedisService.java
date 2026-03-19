package co.com.clients.redis.service;

public interface RedisService {

	void saveToCache(String key, Object value, Long ttl);

	Object getFromCache(String key);

	boolean existsInCache(String key);

	void deleteFromCache(String key);
	
	Long getTTL(String key);
	
}
