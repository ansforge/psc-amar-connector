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
package fr.ans.psc.asynclistener;


import fr.ans.psc.rabbitmq.conf.PscRabbitMqConfiguration;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

//import fr.ans.psc.asynclistener.config.DLQAmqpConfiguration;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public MessageProducer(final RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

//    public void sendContactMessage(String message) {
//        log.info("Sending message...");
//        rabbitTemplate.convertAndSend(DLQAmqpConfiguration.EXCHANGE_MESSAGES, DLQAmqpConfiguration.ROUTING_KEY_MESSAGES_QUEUE, message);
//    }

    public void sendPsMessage(String routingKey, String message) {
        log.info("Sending Ps message...");
        rabbitTemplate.convertAndSend(PscRabbitMqConfiguration.EXCHANGE_MESSAGES, routingKey, message);
    }
}
