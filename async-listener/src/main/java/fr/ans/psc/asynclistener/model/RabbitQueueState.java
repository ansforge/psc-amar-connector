package fr.ans.psc.asynclistener.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.amqp.core.QueueInformation;

@Getter
@Setter
public class RabbitQueueState {
    private String queueName;

    private Integer messageCount;

    private Integer consumerCount;

    public RabbitQueueState(QueueInformation queueInfo, String queueName) {
        this.queueName = queueName;
        this.messageCount = queueInfo.getMessageCount();
        this.consumerCount = queueInfo.getConsumerCount();
    }
}
