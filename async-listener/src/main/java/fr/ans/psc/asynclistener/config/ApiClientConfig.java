package fr.ans.psc.asynclistener.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.ans.psc.ApiClient;

@Configuration
public class ApiClientConfig {

	
	@Value("${in.api.url:http://localhost}")
	private String inApiUrl;
	
	@Value("${psc.api.url:http://localhost}")
	private String pscApiUrl;

	@Bean
	public ApiClient apiclient() {
		ApiClient client = new ApiClient();
		client.setBasePath(pscApiUrl);
		return client;

	}
	
	@Bean
	public fr.ans.in.user.ApiClient inApiclient() {
		fr.ans.in.user.ApiClient client = new fr.ans.in.user.ApiClient();
		client.setBasePath(inApiUrl);
		return client;

	}
}
