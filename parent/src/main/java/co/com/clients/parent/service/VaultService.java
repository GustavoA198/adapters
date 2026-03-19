package co.com.clients.parent.service;

import java.util.Map;

public interface VaultService {

    public Map<String, String> loadByPrefix(String prefix);
}
