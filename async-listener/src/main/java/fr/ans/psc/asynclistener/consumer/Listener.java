/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.asynclistener.consumer;

//import static fr.ans.psc.asynclistener.config.DLQAmqpConfiguration.QUEUE_CONTACT_MESSAGES;
//import static fr.ans.psc.asynclistener.config.DLQAmqpConfiguration.QUEUE_PS_MESSAGES;
//import static java.nio.charset.StandardCharsets.UTF_8;

import fr.ans.psc.ApiClient;
import fr.ans.psc.amar.model.User;
import fr.ans.psc.api.PsApi;
import fr.ans.psc.asynclistener.model.AmarUserAdapter;
import fr.ans.psc.model.Ps;
import fr.ans.psc.rabbitmq.conf.PscRabbitMqConfiguration;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.google.gson.Gson;

import fr.ans.in.user.api.UserApi;
import fr.ans.in.user.model.ContactInfos;
import fr.ans.psc.asynclistener.model.ContactInfosWithNationalId;
import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static fr.ans.psc.rabbitmq.conf.PscRabbitMqConfiguration.QUEUE_PS_CREATE_MESSAGES;
import static fr.ans.psc.rabbitmq.conf.PscRabbitMqConfiguration.QUEUE_PS_UPDATE_MESSAGES;
import static fr.ans.psc.rabbitmq.conf.PscRabbitMqConfiguration.QUEUE_PS_DELETE_MESSAGES;

/**
 * The Class Listener.
 */

@Configuration
@Slf4j
public class Listener {

    private final RabbitTemplate rabbitTemplate;

    private final ApiClient client;

    private final fr.ans.in.user.ApiClient inClient;

    private final fr.ans.psc.amar.ApiClient amarClient;

    private PsApi psapi;

    private UserApi userApi;

    private fr.ans.psc.amar.api.UserApi amarUserApi;

    @Value("${amar.production.ready:false}")
    private boolean isProduction;


    /**
     * The json.
     */
    Gson json = new Gson();

    /**
     * Instantiates a new receiver.
     *
     * @param client         the client
     * @param inClient       the in client
     * @param rabbitTemplate the rabbit template
     */
    public Listener(ApiClient client, fr.ans.in.user.ApiClient inClient,
                    fr.ans.psc.amar.ApiClient amarClient, RabbitTemplate rabbitTemplate) {
        super();
        this.rabbitTemplate = rabbitTemplate;
        this.client = client;
        this.inClient = inClient;
        this.amarClient = amarClient;
        init();
    }

    private void init() {
        psapi = new PsApi(client);
        userApi = new UserApi(inClient);
        amarUserApi = new fr.ans.psc.amar.api.UserApi(amarClient);
    }

//    /**
//     * Dlq amqp container.
//     *
//     * @return the DLQ contact info amqp container
//     */
//    @Bean
//    public DLQContactInfoAmqpContainer dlqAmqpContainer() {
//        return new DLQContactInfoAmqpContainer(rabbitTemplate);
//    }
//
//    /**
//     * process message (Update the PS or create it if not exists). Update structure
//     * as well.
//     *
//     * @param message the message
//     * @throws PscUpdateException the psc update exception
//     */
//    @RabbitListener(queues = QUEUE_PS_MESSAGES)
//	public void receivePsMessage(Message message) throws PscUpdateException {
//		String messageBody = new String(message.getBody());
//		Ps ps = json.fromJson(messageBody, Ps.class);
//		try {
//			psapi.createNewPs(ps);
//		} catch (RestClientException e) {
//			log.error("PS {} not updated in DB.", ps.getNationalId());
//			log.error("Error : ", e);
//		}
//	}

    // TODO PsCreate, PsUpdate : same method cause we use
    @RabbitListener(queues = {QUEUE_PS_CREATE_MESSAGES, QUEUE_PS_UPDATE_MESSAGES})
    public void receivePsCreateAMARMessage(Message message) {
        // get last stored Ps in API
        String messageBody = new String(message.getBody());
        Ps queuedPs = json.fromJson(messageBody, Ps.class);
        Ps storedPs = null;
        try {
            storedPs = psapi.getPsById(URLEncoder.encode(queuedPs.getNationalId(), StandardCharsets.UTF_8));
        } catch (RestClientResponseException e) {
            log.info("Ps {} does not exist in sec-psc database, will not be sent to AMAR", queuedPs.getNationalId());
            return;

        }

        // AMAR call
        try {
            // map Ps with AMAR model
            AmarUserAdapter amarUser = new AmarUserAdapter(storedPs);
            // call amar client : post /put
            if (isProduction) {
                // we should use the PUT method because we want the job done without checking
                amarUserApi.updateUser(amarUser, URLEncoder.encode(amarUser.getNationalId(), StandardCharsets.UTF_8));
                if (message.getMessageProperties().getReceivedRoutingKey().equals(
                        PscRabbitMqConfiguration.PS_CREATE_MESSAGES_QUEUE_ROUTING_KEY)) {
                    log.info("toto");
                }
                log.debug("PS {} successfully stored in AMAR", queuedPs.getNationalId());
            } else {
                log.debug("PS {} successfully mapped", json.toJson(amarUser, User.class));
            }

        } catch (RestClientException e) {
            log.error("PS {} not stored in AMAR", queuedPs.getNationalId());
        }
    }

    // TODO PsUpdate
    @RabbitListener(queues = QUEUE_PS_UPDATE_MESSAGES)
    public void receivePsUpdateAMARMessage(Message message) {
        // get last stored Ps in API

        // map Ps with AMAR model

        // call amar client : put

        // log result
    }

    // TODO PsDelete
    @RabbitListener(queues = QUEUE_PS_DELETE_MESSAGES)
    public void receivePsDeleteAMARMessage(Message message) {
        // get last stored Ps in API

        // map Ps with AMAR model

        // call amar client : delete if absent

        // log result
    }

//	/**
//	 * process message : Update mail and phone number in database ans push modification to IN Api.
//	 *
//	 * @param message the message
//	 * @throws PscUpdateException the psc update exception
//	 */
//    @RabbitListener(queues = QUEUE_CONTACT_MESSAGES)
//	public void receiveContactMessage(Message message) throws PscUpdateException {
//    	log.info("Receiving message...");
//		String messageBody = new String(message.getBody());
//		ContactInfosWithNationalId contactInput = json.fromJson(messageBody, ContactInfosWithNationalId.class);
//		// Get the PS to update
//		String psId = URLEncoder.encode(contactInput.getNationalId(), UTF_8);
//		Ps ps = psapi.getPsById(psId);
//		ps.setEmail(contactInput.getEmail());
//		ps.setPhone(contactInput.getPhone());
//		// Update PS in DB
//		try {
//			psapi.updatePs(ps);
//			log.info("Contact informations sent to API : {}", messageBody);
//		} catch (RestClientResponseException e) {
//			log.error("Contact infos of PS {} not updated in DB (Not requeued) return code : {} ; content : {}", ps.getNationalId(), e.getRawStatusCode(), messageBody);
//			//Exit because we don't want to desynchronize PSC DB and IN data
//			return;
//		} catch (Exception e) {
//			log.error("PS {} not updated in DB (It is requeued).", ps.getNationalId(), e);
//			// Throw exception to requeue message
//				throw new PscUpdateException(e);
//		}
//
//		try {
//			ContactInfos contactOutput = new ContactInfos();
//			contactOutput.setEmail(contactInput.getEmail());
//			contactOutput.setPhone(contactInput.getPhone());
//			String encodedNationalId = URLEncoder.encode(contactInput.getNationalId(), UTF_8);
//			userApi.putUsersContactInfos(encodedNationalId, contactOutput);
//			log.info("Contact informations sent to IN : {}", messageBody);
//		} catch (RestClientResponseException e) {
//			log.error("PS {} not updated at IN : return code {}.", ps.getNationalId(), e.getRawStatusCode());
//		} catch (RestClientException e) {
//			log.error("PS {} not updated at IN (It is requeued).", ps.getNationalId(), e);
//			// Throw exception to requeue message
//				throw new PscUpdateException(e);
//		}
//	}

}
