package co.com.clients.parent.service;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.vault.core.env.LeaseAwareVaultPropertySource;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Clase que se encarga de obtener las propiedades del Vault para Neurona.
 */
@Component
public class VaultServiceImpl implements VaultService{

    private final ConfigurableEnvironment environment;

    /**
     * Inyectamos el Environment y comprobamos que sea Configurable
     */
    public VaultServiceImpl(Environment env) {
        if (!(env instanceof ConfigurableEnvironment)) {
            throw new IllegalStateException("Se necesita ConfigurableEnvironment");
        }
        this.environment = (ConfigurableEnvironment) env;
    }

    /**
     * Lee en caliente todas las propiedades de Vault que empiecen por el prefijo dado,
     * elimina ese prefijo de la clave y devuelve un Map<String,String>.
     *
     * @param prefix El prefijo de las keys en Vault (p.ej. "neurona.trans-origin-ext.data.")
     */
    public Map<String, String> loadByPrefix(String prefix) {
        return StreamSupport.stream(
                        environment.getPropertySources().spliterator(),
                        false
                )
                .filter(ps -> ps instanceof LeaseAwareVaultPropertySource)
                .flatMap(ps -> {
                    LeaseAwareVaultPropertySource vaultSource = (LeaseAwareVaultPropertySource) ps;
                    // AquÃ­ convertimos el String[] en Stream<String>
                    return Arrays.stream(vaultSource.getPropertyNames())
                            .filter(name -> name.startsWith(prefix))
                            .map(name -> Map.entry(
                                    name.substring(prefix.length()),
                                    (String) vaultSource.getProperty(name)
                            ));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
