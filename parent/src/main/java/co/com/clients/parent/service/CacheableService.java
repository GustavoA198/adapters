package co.com.clients.parent.service;

import java.util.Optional;

import co.com.clients.parent.exception.BackendException;

/**
* @author Zathura Code Generator Version 24.05 http://zathuracode.org/
* 
* @generationDate 2024-08-05T08:45:56.708048
*
*/

public interface CacheableService {

	<T> Optional<T> getFromCache(String catalogTable, Class<T> classType, String... params) throws BackendException;

}
