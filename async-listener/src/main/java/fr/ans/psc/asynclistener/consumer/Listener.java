/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.asynclistener.consumer;

import static fr.ans.psc.asynclistener.config.SimpleDLQAmqpConfiguration.QUEUE_CONTACT_MESSAGES;
import static fr.ans.psc.asynclistener.config.SimpleDLQAmqpConfiguration.QUEUE_PS_MESSAGES;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.google.gson.Gson;

import fr.ans.in.user.api.UserApi;
import fr.ans.psc.ApiClient;
import fr.ans.psc.api.PsApi;
import fr.ans.psc.api.StructureApi;
import fr.ans.psc.asynclistener.component.exception.PscUpdateException;
import fr.ans.psc.asynclistener.model.ContactInfosWithNationalId;
import fr.ans.psc.asynclistener.model.PsAndStructure;
import fr.ans.psc.model.Ps;
import lombok.extern.slf4j.Slf4j;
/**
 * The Class Listener.
 */

@Configuration
@Slf4j
public class Listener {
	
	private final RabbitTemplate rabbitTemplate;

	private final ApiClient client;
	
	private final fr.ans.in.user.ApiClient inClient;
	
	private PsApi psapi;
	
	private StructureApi structureapi;
	
	private UserApi userApi;

	Gson json = new Gson();

	/**
	 * Instantiates a new receiver.
	 *
	 * @param client the client
	 */
	public Listener(ApiClient client, fr.ans.in.user.ApiClient inClient, RabbitTemplate rabbitTemplate) {
		super();
		this.rabbitTemplate = rabbitTemplate;
		this.client = client;
		this.inClient = inClient;
		init();
	}

	private void init() {
		psapi = new PsApi(client);
		structureapi = new StructureApi(client);
		userApi = new UserApi(inClient);
	}

    @Bean
    public DLQCustomAmqpContainer dlqAmqpContainer() {
        return new DLQCustomAmqpContainer(rabbitTemplate);
    }
    
    /**
	 * process message (Update the PS or create it if not exists). Update structure
	 * as well.
	 * 
	 * @param message the message
	 * @throws PscUpdateException
	 */
    @RabbitListener(queues = QUEUE_PS_MESSAGES)
	public void receivePsMessage(Message message) throws PscUpdateException {
		String messageBody = new String(message.getBody());
		PsAndStructure wrapper = json.fromJson(messageBody, PsAndStructure.class);
		try {
			psapi.createNewPs(wrapper.getPs());
			if (null != structureapi.getStructureById(wrapper.getStructure().getStructureTechnicalId())) {
				structureapi.updateStructure(wrapper.getStructure());
			} else {
				structureapi.createNewStructure(wrapper.getStructure());
			}
		}catch (RestClientException e) {
			log.error("PS {} not updated in DB.", wrapper.getPs().getNationalId(), e);
		}
	}

    
	/**
	 * process message : Update mail and phone number in database ans push modification to IN Api.
	 * 
	 * @param message the message
	 * @throws PscUpdateException 
	 */
    @RabbitListener(queues = QUEUE_CONTACT_MESSAGES)
	public void receiveContactMessage(Message message) throws PscUpdateException {
		String messageBody = new String(message.getBody());
		ContactInfosWithNationalId contactInput = json.fromJson(messageBody, ContactInfosWithNationalId.class);
		// Get the PS to update
		Ps ps = psapi.getPsById(contactInput.getNationalId());
		ps.setEmail(contactInput.getEmail());
		ps.setPhone(contactInput.getPhone());
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
			fr.ans.in.user.model.ContactInfos contactOutput = new fr.ans.in.user.model.ContactInfos();
			contactOutput.setEmail(contactInput.getEmail());
			contactOutput.setPhone(contactInput.getPhone());
			userApi.putUsersContactInfos(contactOutput);
		}catch (RestClientResponseException e) {
			log.error("PS {} not updated at IN.", ps.getNationalId(), e.getLocalizedMessage());
		} catch (RestClientException e) {
			log.error("PS {} not updated at IN (It is requeued).", ps.getNationalId(), e);
			// Throw exception to requeue message
				throw new PscUpdateException(e);
		}
	}
	
}
