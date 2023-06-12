package info.touret.bookstore.spring.authorizationserver;

import java.util.Set;

/**
 * Holds the configuration of ONE client.
 * For instance:<br/>
 * <pre>authorization.clients.customer1.clientId=customer1
 * authorization.clients.customer1.clientSecret=secret1
 * authorization.clients.customer1.scopes=book:read,book:write,number:readauthorization.clients.customer1.clientId=customer1
 * authorization.clients.customer1.clientSecret=secret1
 * authorization.clients.customer1.scopes=book:read,book:write,number:read</pre>
 */
public class ClientsConfigurationProperties {
	private String clientId;
	private String clientSecret;
	private Set<String> scopes;


	public String getClientId() {
		return this.clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return this.clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public Set<String> getScopes() {
		return this.scopes;
	}

	public void setScopes(Set<String> scopes) {
		this.scopes = scopes;
	}


}