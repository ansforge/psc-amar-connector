package fr.ans.psc.asynclistener;


import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import fr.ans.psc.asynclistener.config.DLQAmqpConfiguration;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public MessageProducer(final RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendContactMessage(String message) {
        log.info("Sending message...");
        rabbitTemplate.convertAndSend(DLQAmqpConfiguration.EXCHANGE_MESSAGES, DLQAmqpConfiguration.ROUTING_KEY_MESSAGES_QUEUE, message);
    }
}