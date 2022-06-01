/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.asynclistener.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The Class DLQAmqpConfiguration.
 */
@Configuration
public class DLQAmqpConfiguration {

    /** The Constant QUEUE_CONTACT_MESSAGES. */
    public static final String QUEUE_CONTACT_MESSAGES = "contact-queue";

    /** The Constant QUEUE_PS_MESSAGES. */
    public static final String QUEUE_PS_MESSAGES = "ps-queue";

    /** The Constant DLX_EXCHANGE_MESSAGES. */
    public static final String DLX_EXCHANGE_MESSAGES = QUEUE_CONTACT_MESSAGES + ".dlx";

    /** The Constant QUEUE_MESSAGES_DLQ. */
    public static final String QUEUE_MESSAGES_DLQ = QUEUE_CONTACT_MESSAGES + ".dlq";

    /** The Constant EXCHANGE_MESSAGES. */
    public static final String EXCHANGE_MESSAGES = "contact-messages-exchange";

    /** The Constant ROUTING_KEY_MESSAGES_QUEUE. */
    public static final String ROUTING_KEY_MESSAGES_QUEUE = "ROUTING_KEY_CONTACT_MESSAGES_QUEUE";

    /** The Constant QUEUE_PARKING_LOT. */
    public static final String QUEUE_PARKING_LOT = QUEUE_CONTACT_MESSAGES + ".parking-lot";

    /** The Constant EXCHANGE_PARKING_LOT. */
    public static final String EXCHANGE_PARKING_LOT = QUEUE_CONTACT_MESSAGES + "exchange.parking-lot";


    /**
     * Contact messages queue.
     *
     * @return the queue
     */
    @Bean
    Queue contactMessagesQueue() {
        return QueueBuilder.durable(QUEUE_CONTACT_MESSAGES)
        	     .withArgument("x-dead-letter-exchange", DLX_EXCHANGE_MESSAGES)
        	      .build();
    }

    /**
     * Messages exchange.
     *
     * @return the direct exchange
     */
    @Bean
    DirectExchange messagesExchange() {
        return new DirectExchange(EXCHANGE_MESSAGES);
    }

    /**
     * Binding messages.
     *
     * @return the binding
     */
    @Bean
    Binding bindingMessages() {
        return BindingBuilder.bind(contactMessagesQueue()).to(messagesExchange()).with(ROUTING_KEY_MESSAGES_QUEUE);
    }

    /**
     * Dead letter exchange.
     *
     * @return the fanout exchange
     */
    @Bean
    FanoutExchange deadLetterExchange() {
        return new FanoutExchange(DLX_EXCHANGE_MESSAGES);
    }

    /**
     * Dead letter queue.
     *
     * @return the queue
     */
    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder.durable(QUEUE_MESSAGES_DLQ).build();
    }

    /**
     * Dead letter binding.
     *
     * @return the binding
     */
    @Bean
    Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange());
    }

    /**
     * Parking lot exchange.
     *
     * @return the fanout exchange
     */
    @Bean
    FanoutExchange parkingLotExchange() {
        return new FanoutExchange(EXCHANGE_PARKING_LOT);
    }

    /**
     * Parking lot queue.
     *
     * @return the queue
     */
    @Bean
    Queue parkingLotQueue() {
        return QueueBuilder.durable(QUEUE_PARKING_LOT).build();
    }

    /**
     * Parking lot binding.
     *
     * @return the binding
     */
    @Bean
    Binding parkingLotBinding() {
        return BindingBuilder.bind(parkingLotQueue()).to(parkingLotExchange());
    }
}
