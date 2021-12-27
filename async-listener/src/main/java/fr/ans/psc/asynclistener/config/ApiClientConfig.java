/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.asynclistener.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.ans.psc.ApiClient;

/**
 * The Class ApiClientConfig.
 */
@Configuration
public class ApiClientConfig {

	
	@Value("${in.api.url:http://localhost/api/lura/ing/rass}")
	private String inApiUrl;
	
	@Value("${psc.api.url:http://localhost/api}")
	private String pscApiUrl;

	/**
	 * Apiclient.
	 *
	 * @return the api client
	 */
	@Bean
	public ApiClient apiclient() {
		ApiClient client = new ApiClient();
		client.setBasePath(pscApiUrl);
		return client;

	}
	
	/**
	 * In apiclient.
	 *
	 * @return the fr.ans.in.user. api client
	 */
	@Bean
	public fr.ans.in.user.ApiClient inApiclient() {
		fr.ans.in.user.ApiClient client = new fr.ans.in.user.ApiClient();
		client.setBasePath(inApiUrl);
		return client;

	}
}
