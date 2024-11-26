/*
 * Copyright © 2022-2024 Agence du Numérique en Santé (ANS) (https://esante.gouv.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

	@Value("${in.amar.url:http://localhost/api/lura}")
	private String amarApiUrl;

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

	@Bean
	public fr.ans.psc.amar.ApiClient amarApiClient() {
		fr.ans.psc.amar.ApiClient client = new fr.ans.psc.amar.ApiClient();
		client.setBasePath(amarApiUrl);
		return client;
	}
}
