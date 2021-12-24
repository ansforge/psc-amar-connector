package fr.ans.psc.asynclistener;


import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import fr.ans.psc.asynclistener.config.SimpleDLQAmqpConfiguration;
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
        rabbitTemplate.convertAndSend(SimpleDLQAmqpConfiguration.EXCHANGE_MESSAGES, SimpleDLQAmqpConfiguration.QUEUE_CONTACT_MESSAGES, message);
    }
}