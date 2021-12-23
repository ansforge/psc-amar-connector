/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.asynclistener.component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.RequestEntity.BodyBuilder;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

import fr.ans.psc.ApiClient;
import fr.ans.psc.api.PsApi;
import fr.ans.psc.asynclistener.component.exception.PscUpdateException;
import fr.ans.psc.asynclistener.model.ContactInfos;
import fr.ans.psc.model.Ps;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class Receiver.
 */
@RabbitListener(queues = "${contact.queue.name:contact-queue}")
@Component
@Slf4j
public class ContactInfosReceiver {

	private final ApiClient client;
	
	private PsApi psapi;
	
	@Value("in.api.url")
	private String inApiUrl;

	private final CountDownLatch latch = new CountDownLatch(1);

	Gson json = new Gson();

	/**
	 * Instantiates a new receiver.
	 *
	 * @param client the client
	 */
	public ContactInfosReceiver(ApiClient client) {
		super();
		this.client = client;
		init();
	}

	private void init() {
		psapi = new PsApi(client);
	}

	/**
	 * process message : Update mail and phone number in database ans push modification to IN Api.
	 * 
	 * @param message the message
	 * @throws PscUpdateException 
	 */
	@RabbitHandler
	public void receiveMessage(String message) throws PscUpdateException {
		ContactInfos contactInfos = json.fromJson(message, ContactInfos.class);
		// Get the PS to update
		Ps ps = psapi.getPsById(contactInfos.getNationalId());
		ps.setEmail(contactInfos.getEmail());
		ps.setPhone(contactInfos.getPhone());
		// Update PS in DB
		try {
			psapi.updatePs(ps);
		}catch (RestClientResponseException e) {
			log.error("PS {} not updated in DB.", ps.getNationalId(), e.getLocalizedMessage());
		} catch (RestClientException e) {
			log.error("PS {} not updated in DB (It is requeued).", ps.getNationalId(), e);
			// Throw exception to requeue message
				throw new PscUpdateException(e);
		}
		
		try {
			RestTemplate restTemplate = new RestTemplate();
			// This allows us to read the response more than once - Necessary for debugging.
			restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(restTemplate.getRequestFactory()));
			restTemplate.exchange(buildRequest(contactInfos), Void.class);
		}catch (RestClientResponseException e) {
			log.error("PS {} not updated at IN.", ps.getNationalId(), e.getLocalizedMessage());
		} catch (RestClientException | URISyntaxException e) {
			log.error("PS {} not updated at IN (It is requeued).", ps.getNationalId(), e);
			// Throw exception to requeue message
			//TODO use dead letter queue strategy : https://www.baeldung.com/spring-amqp-error-handling
				throw new PscUpdateException(e);
		}
		
		latch.countDown();
	}

	public CountDownLatch getLatch() {
		return latch;
	}
	
	private RequestEntity<Object> buildRequest(ContactInfos contactInfos) throws URISyntaxException{
		final BodyBuilder requestBuilder = RequestEntity.method(HttpMethod.PUT, new URI(inApiUrl));
            requestBuilder.accept(new MediaType(MediaType.APPLICATION_JSON));
            requestBuilder.contentType(MediaType.APPLICATION_JSON);
       String body = json.toJson(contactInfos);
        return requestBuilder.body(body);
	}

}
