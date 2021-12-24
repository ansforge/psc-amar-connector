package fr.ans.psc.asynclistener.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SimpleDLQAmqpConfiguration {
    public static final String QUEUE_CONTACT_MESSAGES = "contact-queue";
    public static final String QUEUE_PS_MESSAGES = "ps-queue";
    public static final String DLX_EXCHANGE_MESSAGES = QUEUE_CONTACT_MESSAGES + ".dlx";
    public static final String QUEUE_MESSAGES_DLQ = QUEUE_CONTACT_MESSAGES + ".dlq";
    public static final String EXCHANGE_MESSAGES = "contact-messages-exchange";

    @Bean
    Queue contactMessagesQueue() {
        return QueueBuilder.durable(QUEUE_CONTACT_MESSAGES)
          .withArgument("x-dead-letter-exchange", DLX_EXCHANGE_MESSAGES)
          .build();
    }

    @Bean
    Queue psMessagesQueue() {
        return QueueBuilder.durable(QUEUE_PS_MESSAGES)
          .build();
    }
    
    @Bean
    FanoutExchange deadLetterExchange() {
        return new FanoutExchange(DLX_EXCHANGE_MESSAGES);
    }

    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder.durable(QUEUE_MESSAGES_DLQ).build();
    }

    @Bean
    Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange());
    }

    @Bean
    DirectExchange messagesExchange() {
        return new DirectExchange(EXCHANGE_MESSAGES);
    }

    @Bean
    Binding bindingMessages() {
        return BindingBuilder.bind(contactMessagesQueue()).to(messagesExchange()).with(QUEUE_CONTACT_MESSAGES);
    }
}