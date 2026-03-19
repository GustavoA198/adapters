package co.com.clients.redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class RedisClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedisClientApplication.class, args);
	}

}
