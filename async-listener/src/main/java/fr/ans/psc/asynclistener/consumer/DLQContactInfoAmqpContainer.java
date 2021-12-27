/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.asynclistener.consumer;

import static fr.ans.psc.asynclistener.config.DLQAmqpConfiguration.EXCHANGE_MESSAGES;
import static fr.ans.psc.asynclistener.config.DLQAmqpConfiguration.QUEUE_MESSAGES_DLQ;
import static fr.ans.psc.asynclistener.config.DLQAmqpConfiguration.QUEUE_PARKING_LOT;
import static fr.ans.psc.asynclistener.config.DLQAmqpConfiguration.EXCHANGE_PARKING_LOT;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import lombok.extern.slf4j.Slf4j;


/**
 * The Class DLQContactInfoAmqpContainer.
 */
@Slf4j
public class DLQContactInfoAmqpContainer {

	/** The Constant HEADER_X_RETRIES_COUNT. */
	public static final String HEADER_X_RETRIES_COUNT = "x-retries-count";
    private final RabbitTemplate rabbitTemplate;
    
    /** The Constant MAX_RETRIES_COUNT. */
    public static final int MAX_RETRIES_COUNT = 2;

    /**
     * Instantiates a new DLQ contact info amqp container.
     *
     * @param rabbitTemplate the rabbit template
     */
    public DLQContactInfoAmqpContainer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Process failed messages retry headers.
     *
     * @param failedMessage the failed message
     */
    @RabbitListener(queues = QUEUE_MESSAGES_DLQ)
    public void processFailedMessagesRetryHeaders(Message failedMessage) {
        Integer retriesCnt = (Integer) failedMessage.getMessageProperties().getHeaders().get(HEADER_X_RETRIES_COUNT);
        if (retriesCnt == null)
            retriesCnt = 1;
        if (retriesCnt > MAX_RETRIES_COUNT) {
        	log.info("Sending message to the parking lot queue");
            rabbitTemplate.send(EXCHANGE_PARKING_LOT, 
              failedMessage.getMessageProperties().getReceivedRoutingKey(), failedMessage);
            return;
        }
        log.info("Retrying message for the {} time", retriesCnt);
        failedMessage.getMessageProperties().getHeaders().put(HEADER_X_RETRIES_COUNT, ++retriesCnt);
        rabbitTemplate.send(EXCHANGE_MESSAGES, failedMessage.getMessageProperties().getReceivedRoutingKey(), failedMessage);
    }
}