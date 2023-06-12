package info.touret.bookstore.spring.authorizationserver;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Holds the configuration of all the clients defined in the <pre>application.properties</pre> file
 *
 * @see ClientsConfigurationProperties
 */
@Configuration
@ConfigurationProperties(prefix = "authorization")
public class AuthorizationClientsProperties {

    private Map<String, ClientsConfigurationProperties> clients;

    @ConfigurationProperties
    public Map<String, ClientsConfigurationProperties> getClients() {
        return clients;
    }

    public void setClients(Map<String, ClientsConfigurationProperties> clients) {
        this.clients = clients;
    }

}