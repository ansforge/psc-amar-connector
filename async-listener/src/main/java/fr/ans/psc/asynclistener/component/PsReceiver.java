/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.asynclistener.component;

import java.util.concurrent.CountDownLatch;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.google.gson.Gson;

import fr.ans.psc.ApiClient;
import fr.ans.psc.api.PsApi;
import fr.ans.psc.api.StructureApi;
import fr.ans.psc.asynclistener.component.exception.PscUpdateException;
import fr.ans.psc.asynclistener.model.PsAndStructure;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class Receiver.
 */
@RabbitListener(queues = "${file.queue.name:file-ps-queue}")
@Component
@Slf4j
public class PsReceiver {

	private final ApiClient client;

	private PsApi psapi;

	private StructureApi structureapi;

	private final CountDownLatch latch = new CountDownLatch(1);

	private Gson json = new Gson();

	/**
	 * Instantiates a new receiver.
	 *
	 * @param client the client
	 */
	public PsReceiver(ApiClient client) {
		super();
		this.client = client;
		init();
	}

	private void init() {
		psapi = new PsApi(client);
		structureapi = new StructureApi(client);
	}

	/**
	 * process message (Update the PS or create it if not exists). Update structure
	 * as well.
	 * 
	 * @param message the message
	 * @throws PscUpdateException
	 */
	@RabbitHandler
	public void receiveMessage(String message) throws PscUpdateException {
		PsAndStructure wrapper = json.fromJson(message, PsAndStructure.class);
		try {
			psapi.createNewPs(wrapper.getPs());
			if (null != structureapi.getStructureById(wrapper.getStructure().getStructureTechnicalId())) {
				structureapi.updateStructure(wrapper.getStructure());
			} else {
				structureapi.createNewStructure(wrapper.getStructure());
			}
		}catch (RestClientResponseException e) {
			log.error("PS {} not updated in DB.", wrapper.getPs().getNationalId(), e);
		} catch (RestClientException e) {
			log.error("PS {} not updated in DB (It is requeued).", wrapper.getPs().getNationalId(), e);
			// Throw exception to requeue message
				throw new PscUpdateException(e);
		}
		latch.countDown();
	}

	public CountDownLatch getLatch() {
		return latch;
	}

}
