package co.com.clients.parent.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
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
public class CacheableAuxServiceImpl implements CacheableAuxService {
	
	@Cacheable(value = "#catalog")
	public List<Object> getCatalog(String catalog) {
		return null;
	}
	
}
