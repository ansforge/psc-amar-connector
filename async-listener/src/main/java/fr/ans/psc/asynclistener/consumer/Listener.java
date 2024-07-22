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
package fr.ans.psc.asynclistener.consumer;

import fr.ans.psc.ApiClient;
import fr.ans.psc.api.PsApi;
import fr.ans.psc.asynclistener.model.AmarUserAdapter;
import fr.ans.psc.model.Ps;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static fr.ans.psc.rabbitmq.conf.PscRabbitMqConfiguration.*;

/**
 * The Class Listener.
 */

@Configuration
@Slf4j
public class Listener {

    private final RabbitTemplate rabbitTemplate;

    private final ApiClient client;

    private final fr.ans.psc.amar.ApiClient amarClient;

    private PsApi psapi;

    private fr.ans.psc.amar.api.UserApi amarUserApi;

    @Value("${amar.production.ready:false}")
    private boolean isSendToAMAR;

    private final String OTHER_IDS = "otherIds";

    private MsgTimeChecker msgTimeChecker;


    /**
     * The json.
     */
    Gson json = new Gson();

    /**
     * Instantiates a new receiver.
     *
     * @param client         the client
     * @param rabbitTemplate the rabbit template
     */
    public Listener(ApiClient client, fr.ans.psc.amar.ApiClient amarClient, RabbitTemplate rabbitTemplate) {
        super();
        this.rabbitTemplate = rabbitTemplate;
        this.client = client;
        this.amarClient = amarClient;
        init();
    }

    private void init() {
        psapi = new PsApi(client);
        amarUserApi = new fr.ans.psc.amar.api.UserApi(amarClient);
        msgTimeChecker = MsgTimeChecker.getInstance();
    }

    @RabbitListener(queues = QUEUE_PS_CREATE_MESSAGES)
    public void receivePsCreateAMARMessage(Message message) {
        log.info("Starting message consuming");
        msgTimeChecker.setMsgConsumptionTimestamp();
        // get last stored Ps in API
        String messageBody = new String(message.getBody());
        log.info("Message body : {}", messageBody);
        Ps queuedPs = json.fromJson(messageBody, Ps.class);
        Ps storedPs;

        // 3 possibilities while getting Ps from sec-psc db :
        // API sends back a 200 http status code (no RestClientResponseException raised) : we continue to AMAR
        // API sends back a 410 http status code : no Ps activated in our db, we just exit
        // API sends back any other "failing" code (400, 404, 500) : we move message to parking lot
        try {
            storedPs = psapi.getPsById(URLEncoder.encode(queuedPs.getNationalId(), StandardCharsets.UTF_8), OTHER_IDS);
            if (storedPs == null) {
                log.error("Stored Ps not correctly pulled");
            }
        } catch (RestClientResponseException e) {
            if (e.getRawStatusCode() == HttpStatus.GONE.value()) {
                log.info("Ps {} does not exist in sec-psc database, will not be sent to AMAR", queuedPs.getNationalId());
            } else {
                log.info("API error, Ps {} is pushed to parking lot for latter treatment", queuedPs.getNationalId());
                rabbitTemplate.send(DLX_EXCHANGE_MESSAGES, message.getMessageProperties().getReceivedRoutingKey(), message);
            }
            return;
        }

        // AMAR call
        try {
            log.info("Converting Ps {} to AMAR format", queuedPs.getNationalId());
            // map Ps with AMAR model
            AmarUserAdapter amarUser = new AmarUserAdapter(storedPs);
            // call amar client : post /put
            if (isSendToAMAR) {
                // we should use the PUT method because we want the job done without checking
                amarUserApi.createUser(amarUser);
                log.debug("PS {} successfully stored in AMAR, routing key was {}",
                        queuedPs.getNationalId(),
                        message.getMessageProperties().getReceivedRoutingKey());
            } else {
                log.info("PS {} successfully created in test env", queuedPs.getNationalId());
                log.debug("Amar User looked like : {}", amarUser);
            }
            // We should never get a 409 http status code, because as AMAR doc states, the update method updates
            // or stores if the Ps does not exist yet
            // if it would change in the future, then we would need to add a RestResponseClientException catch clause
            // to check the raw status code and not send message to parking lot if 409
        } catch (RestClientException e) {
            log.warn("PS {} not stored in AMAR, moved to dead letter queue", queuedPs.getNationalId());
            log.error("AMAR side Exception", e);
            rabbitTemplate.send(DLX_EXCHANGE_MESSAGES, message.getMessageProperties().getReceivedRoutingKey(), message);
        } catch (Exception e) {
            log.error("An exception occurred", e);
            rabbitTemplate.send(DLX_EXCHANGE_MESSAGES, message.getMessageProperties().getReceivedRoutingKey(), message);
        }
    }

    @RabbitListener(queues = QUEUE_PS_UPDATE_MESSAGES)
    public void receivePsUpdateAMARMessage(Message message) {
        log.info("Starting message consuming");
        msgTimeChecker.setMsgConsumptionTimestamp();
        // get last stored Ps in API
        String messageBody = new String(message.getBody());
        log.info("Message body : {}", messageBody);
        Ps queuedPs = json.fromJson(messageBody, Ps.class);
        Ps storedPs;

        // 3 possibilities while getting Ps from sec-psc db :
        // API sends back a 200 http status code (no RestClientResponseException raised) : we continue to AMAR
        // API sends back a 410 http status code : no Ps activated in our db, we just exit
        // API sends back any other "failing" code (400, 404, 500) : we move message to parking lot
        try {
            storedPs = psapi.getPsById(URLEncoder.encode(queuedPs.getNationalId(), StandardCharsets.UTF_8), OTHER_IDS);
            if (storedPs == null) {
                log.error("Stored Ps not correctly pulled");
            }
        } catch (RestClientResponseException e) {
            if (e.getRawStatusCode() == HttpStatus.GONE.value()) {
                log.info("Ps {} does not exist in sec-psc database, will not be sent to AMAR", queuedPs.getNationalId());
            } else {
                log.info("API error, Ps {} is pushed to parking lot for latter treatment", queuedPs.getNationalId());
                rabbitTemplate.send(DLX_EXCHANGE_MESSAGES, message.getMessageProperties().getReceivedRoutingKey(), message);
            }
            return;
        }

        // AMAR call
        try {
            log.info("Converting Ps {} to AMAR format", queuedPs.getNationalId());
            // map Ps with AMAR model
            AmarUserAdapter amarUser = new AmarUserAdapter(storedPs);
            // call amar client : post /put
            if (isSendToAMAR) {
                // we should use the PUT method because we want the job done without checking
                amarUserApi.updateUser(amarUser, URLEncoder.encode(amarUser.getNationalId(), StandardCharsets.UTF_8));
                log.debug("PS {} successfully stored in AMAR, routing key was {}",
                        queuedPs.getNationalId(),
                        message.getMessageProperties().getReceivedRoutingKey());
            } else {
                log.info("PS {} successfully updated in test env", queuedPs.getNationalId());
                log.debug("Amar User looked like : {}", amarUser);
            }
            // We should never get a 409 http status code, because as AMAR doc states, the update method updates
            // or stores if the Ps does not exist yet
            // if it would change in the future, then we would need to add a RestResponseClientException catch clause
            // to check the raw status code and not send message to parking lot if 409
        } catch (RestClientException e) {
            log.warn("PS {} not stored in AMAR, moved to dead letter queue", queuedPs.getNationalId());
            log.error("AMAR side Exception was", e);
            rabbitTemplate.send(DLX_EXCHANGE_MESSAGES, message.getMessageProperties().getReceivedRoutingKey(), message);
        } catch (Exception e) {
            log.error("An exception occurred", e);
            rabbitTemplate.send(DLX_EXCHANGE_MESSAGES, message.getMessageProperties().getReceivedRoutingKey(), message);
        }
    }

    @RabbitListener(queues = QUEUE_PS_DELETE_MESSAGES)
    public void receivePsDeleteAMARMessage(Message message) {
        msgTimeChecker.setMsgConsumptionTimestamp();
        // get last stored Ps in API
        String messageBody = new String(message.getBody());
        Ps queuedPs = json.fromJson(messageBody, Ps.class);
        Ps storedPs;

        // 3 possibilities while getting Ps from sec-psc db :
        // API sends back a 200 http status code (no RestClientResponseException raised) : Ps still exists, we exit
        // API sends back a 410 http status code : no Ps activated in our db, we continue to AMAR
        // API sends back any other "failing" code (400, 404, 500) : we move message to parking lot
        try {
            storedPs = psapi.getPsById(URLEncoder.encode(queuedPs.getNationalId(), StandardCharsets.UTF_8), OTHER_IDS);
            if (storedPs != null) {
                log.info("Ps {} still exists in sec-psc database, will not be sent to AMAR", queuedPs.getNationalId());
                return;
            }
        } catch (RestClientResponseException e) {
            if (e.getRawStatusCode() != HttpStatus.GONE.value()) {
                log.info("API error, Ps {} is pushed to dead letter queue for later treatment", queuedPs.getNationalId());
                rabbitTemplate.send(DLX_EXCHANGE_MESSAGES, PS_DELETE_MESSAGES_QUEUE_ROUTING_KEY, message);
                return;
            }
        }

        // AMAR call
        try {
            if (isSendToAMAR) {
                amarUserApi.deleteUser(URLEncoder.encode(queuedPs.getNationalId(), StandardCharsets.UTF_8));
                log.debug("PS {} successfully deleted in AMAR",
                        queuedPs.getNationalId());
            } else {
                log.info("PS {} successfully mapped in test env", queuedPs.getNationalId());
            }

            // call amar client : delete if absent
        } catch (RestClientResponseException e) {
            if (e.getRawStatusCode() == HttpStatus.NOT_FOUND.value()) {
                log.info("Ps {} already absent in AMAR", queuedPs.getNationalId());
            } else {
                // any error in url, headers, etc
                log.warn("PS {} not deleted in AMAR, moved to dead letter queue", queuedPs.getNationalId());
                rabbitTemplate.send(DLX_EXCHANGE_MESSAGES,PS_DELETE_MESSAGES_QUEUE_ROUTING_KEY, message);
            }
        } catch (RestClientException e) {
            // no connection established
            log.warn("PS {} not deleted in AMAR, moved to dead letter queue", queuedPs.getNationalId());
            log.error("Exception was", e);
            rabbitTemplate.send(DLX_EXCHANGE_MESSAGES, PS_DELETE_MESSAGES_QUEUE_ROUTING_KEY, message);
        } catch (Exception e) {
            log.error("an Exception occured", e);
        }
    }
}
