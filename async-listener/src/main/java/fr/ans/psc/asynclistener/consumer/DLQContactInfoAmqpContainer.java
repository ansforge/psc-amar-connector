package fr.ans.psc.asynclistener.consumer;

import static fr.ans.psc.asynclistener.config.DLQAmqpConfiguration.EXCHANGE_MESSAGES;
import static fr.ans.psc.asynclistener.config.DLQAmqpConfiguration.QUEUE_MESSAGES_DLQ;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class DLQContactInfoAmqpContainer {

	public static final String HEADER_X_RETRIES_COUNT = "x-retries-count";
    private final RabbitTemplate rabbitTemplate;
    public static final int MAX_RETRIES_COUNT = 2;

    public DLQContactInfoAmqpContainer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = QUEUE_MESSAGES_DLQ)
    public void processFailedMessagesRetryHeaders(Message failedMessage) {
        Integer retriesCnt = (Integer) failedMessage.getMessageProperties().getHeaders().get(HEADER_X_RETRIES_COUNT);
        if (retriesCnt == null)
            retriesCnt = 1;
        if (retriesCnt > MAX_RETRIES_COUNT) {
            log.info("Discarding message, content : {}", new String(failedMessage.getBody()));
            // TODO Mail to admin with message content
            return;
        }
        log.info("Retrying message for the {} time", retriesCnt);
        failedMessage.getMessageProperties().getHeaders().put(HEADER_X_RETRIES_COUNT, ++retriesCnt);
        rabbitTemplate.send(EXCHANGE_MESSAGES, failedMessage.getMessageProperties().getReceivedRoutingKey(), failedMessage);
    }
}