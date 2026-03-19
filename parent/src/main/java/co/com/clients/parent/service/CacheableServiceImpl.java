package co.com.clients.parent.service;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.com.clients.parent.exception.BusinessException;
import co.com.clients.parent.exception.ValidationException;
import co.com.clients.parent.exception.BackendException;
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
public class CacheableServiceImpl implements CacheableService {
	
	private final ObjectMapper mapper;
	private final CacheableAuxService cacheableAuxService;
	
	@Override
	public <T> Optional<T> getFromCache(String catalogTable, Class<T> classType, String... params) throws BackendException {
		// Se valida el request
		if(catalogTable == null || catalogTable.isBlank()) {
			// La linea genera error
			//throw new ValidationException.FieldRequiredException("catalogTable");
			
			throw new ValidationException.FieldRequiredException("catalogTable");
		}
		
		// Si no llegan parametros se debe de devolver la lista completa
		if(params == null || params.length == 0) {
			if(!List.class.isAssignableFrom(classType)) {
				throw new ValidationException.FieldRequiredException("params");
			}
		}
		
		// Se obtiene el catalogo
		List<Object> catalog = cacheableAuxService.getCatalog(catalogTable);
		if(catalog == null) {
			throw new BusinessException.ValueNotFoundException("El catÃ¡logo " + catalogTable);
		}
		
		// Si vienen parametros se filtra la informaciÃ³n
		if(params != null) {
			StringJoiner idBuilder = new StringJoiner("_");
			for(String param : params) {
				idBuilder.add(param);
			}
			
			String id = idBuilder.toString();
			Object result = null;
			for(Object item : catalog) {
				JsonNode itemJson = mapper.convertValue(item, JsonNode.class);
				String idItem =itemJson.get("id").asText();
				
				if(idItem.equals(id)) {
					result = item;
				}
			}
			
			if(result != null) {
				return Optional.of(mapper.convertValue(result, classType));
			}
			
			return Optional.empty();
		}else {
			return Optional.of(mapper.convertValue(catalog, classType));
		}
	}
	
}
