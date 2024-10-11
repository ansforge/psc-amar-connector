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
package fr.ans.psc.asynclistener.controller;

import fr.ans.psc.asynclistener.consumer.MsgTimeChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;

import static fr.ans.psc.rabbitmq.conf.PscRabbitMqConfiguration.QUEUE_PS_CREATE_MESSAGES;
import static fr.ans.psc.rabbitmq.conf.PscRabbitMqConfiguration.QUEUE_PS_DELETE_MESSAGES;
import static fr.ans.psc.rabbitmq.conf.PscRabbitMqConfiguration.QUEUE_PS_UPDATE_MESSAGES;

@RestController
public class PscListenerActivityController {
    /**
     * logger.
     */
    private static final Logger log = LoggerFactory.getLogger(PscListenerActivityController.class);

    private final RabbitAdmin rabbitAdmin;

    @Autowired
    public PscListenerActivityController(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    @GetMapping(value = "/check", produces = MediaType.APPLICATION_JSON_VALUE)
    public String index() {
        return "alive";
    }

    @GetMapping(value = "/check-pending-messages", produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean isHandlingMessages() {
        return hasQueuedMessages() || MsgTimeChecker.getInstance().hasRecentConsumptionTimestamp();
    }

    private boolean hasQueuedMessages() {
        ArrayList<String> queues = new ArrayList<>(Arrays.asList(QUEUE_PS_CREATE_MESSAGES, QUEUE_PS_UPDATE_MESSAGES, QUEUE_PS_DELETE_MESSAGES));
        for (String queue : queues) {
            try {
                QueueInformation queueState = this.getQueueState(queue);
                if (queueState.getMessageCount() > 0) {
                    log.info("{} queue has {} messages and {} DLQ queue has consumed {} messages",
                            queue, queueState.getMessageCount(), queue, queueState.getConsumerCount());
                    return true;
                }
            } catch (AmqpException e) {
                log.error("Error occurred when checking the contents of the queue: {} is null", queue, e);
            }
        }

        // all queues are empty
        return false;
    }

    private QueueInformation getQueueState(String queueName) throws AmqpException {
        QueueInformation queueInfo = rabbitAdmin.getQueueInfo(queueName);

        if (queueInfo != null) {
            return queueInfo;
        } else {
            throw new AmqpException("Error occurred when getting message count from " + queueName);
        }
    }
}
