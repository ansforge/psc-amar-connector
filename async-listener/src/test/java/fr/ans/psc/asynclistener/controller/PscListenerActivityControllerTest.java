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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import fr.ans.psc.asynclistener.consumer.MsgTimeChecker;
import fr.ans.psc.asynclistener.utils.MemoryAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static fr.ans.psc.rabbitmq.conf.PscRabbitMqConfiguration.QUEUE_PS_CREATE_MESSAGES;
import static fr.ans.psc.rabbitmq.conf.PscRabbitMqConfiguration.QUEUE_PS_DELETE_MESSAGES;
import static fr.ans.psc.rabbitmq.conf.PscRabbitMqConfiguration.QUEUE_PS_UPDATE_MESSAGES;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ContextConfiguration(classes = PscListenerActivityController.class)
@AutoConfigureMockMvc
class PscListenerActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PscListenerActivityController controller;

    @MockBean
    private RabbitAdmin rabbitAdmin;

    private MemoryAppender memoryAppender;

    @BeforeEach
    public void setup() {
        // LOG APPENDER
        Logger logger = (Logger) LoggerFactory.getLogger(PscListenerActivityController.class);
        memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.setLevel(Level.DEBUG);
        logger.addAppender(memoryAppender);
        memoryAppender.start();
    }

    @AfterEach
    public void tearDown() {
        Logger logger = (Logger) LoggerFactory.getLogger(PscListenerActivityController.class);
        logger.detachAppender(memoryAppender);
        memoryAppender.stop();
    }

    @Test
    void verifyAmarConnectorIsAlive() throws Exception {
        mockMvc.perform(get("/check"))
                .andExpect(status().isOk())
                .andExpect(content().string("alive"));
    }

    @Test
    void testIsHandlingMessagesTrue() {
        QueueInformation createQueueState = new QueueInformation(QUEUE_PS_CREATE_MESSAGES, 10, 5);
        QueueInformation updateQueueState = new QueueInformation(QUEUE_PS_UPDATE_MESSAGES, 20, 10);
        QueueInformation deleteQueueState = new QueueInformation(QUEUE_PS_DELETE_MESSAGES, 30, 15);
        MsgTimeChecker.getInstance().setMsgConsumptionTimestamp();

        when(rabbitAdmin.getQueueInfo(QUEUE_PS_CREATE_MESSAGES))
                .thenReturn(createQueueState);
        when(rabbitAdmin.getQueueInfo(QUEUE_PS_UPDATE_MESSAGES))
                .thenReturn(updateQueueState);
        when(rabbitAdmin.getQueueInfo(QUEUE_PS_DELETE_MESSAGES))
                .thenReturn(deleteQueueState);

        assertTrue(controller.isHandlingMessages());

        boolean hasNoErrorEvents = memoryAppender.getLoggedEvents().stream().allMatch(logEvent -> logEvent.getLevel().equals(Level.INFO));
        assertTrue(hasNoErrorEvents);
        assertThat(memoryAppender.getLoggedEvents()).hasSize(1);
    }

    @Test
    void testIsHandlingMessagesFalseWhenAllQueuesAreEmptyAndNoRecentMessage() {
        QueueInformation createQueueState = new QueueInformation(QUEUE_PS_CREATE_MESSAGES, 0, 0);
        QueueInformation updateQueueState = new QueueInformation(QUEUE_PS_UPDATE_MESSAGES, 0, 0);
        QueueInformation deleteQueueState = new QueueInformation(QUEUE_PS_DELETE_MESSAGES, 0, 0);
        MsgTimeChecker.getInstance().setTimestamp(Instant.now().minus(1, ChronoUnit.MINUTES));

        when(rabbitAdmin.getQueueInfo(QUEUE_PS_CREATE_MESSAGES))
                .thenReturn(createQueueState);
        when(rabbitAdmin.getQueueInfo(QUEUE_PS_UPDATE_MESSAGES))
                .thenReturn(updateQueueState);
        when(rabbitAdmin.getQueueInfo(QUEUE_PS_DELETE_MESSAGES))
                .thenReturn(deleteQueueState);

        assertFalse(controller.isHandlingMessages());
        assertThat(memoryAppender.getLoggedEvents()).isEmpty();
    }

    @Test
    void testIsHandlingMessagesTrueWhenAllQueuesAreEmptyButMessageInWindow() {
        QueueInformation createQueueState = new QueueInformation(QUEUE_PS_CREATE_MESSAGES, 0, 0);
        QueueInformation updateQueueState = new QueueInformation(QUEUE_PS_UPDATE_MESSAGES, 0, 0);
        QueueInformation deleteQueueState = new QueueInformation(QUEUE_PS_DELETE_MESSAGES, 0, 0);
        MsgTimeChecker.getInstance().setMsgConsumptionTimestamp();

        when(rabbitAdmin.getQueueInfo(QUEUE_PS_CREATE_MESSAGES))
                .thenReturn(createQueueState);
        when(rabbitAdmin.getQueueInfo(QUEUE_PS_UPDATE_MESSAGES))
                .thenReturn(updateQueueState);
        when(rabbitAdmin.getQueueInfo(QUEUE_PS_DELETE_MESSAGES))
                .thenReturn(deleteQueueState);

        assertTrue(controller.isHandlingMessages());
        boolean hasNoErrorEvents = memoryAppender.getLoggedEvents().stream().allMatch(logEvent -> logEvent.getLevel().equals(Level.INFO));
        assertTrue(hasNoErrorEvents);
        assertThat(memoryAppender.getLoggedEvents()).hasSize(0);
    }

    @Test()
    void testIsHandlingMessagesFalseWhenQueuesAreNull() {
        MsgTimeChecker.getInstance().setTimestamp(null);

        when(rabbitAdmin.getQueueInfo(QUEUE_PS_CREATE_MESSAGES))
                .thenReturn(null);
        when(rabbitAdmin.getQueueInfo(QUEUE_PS_UPDATE_MESSAGES))
                .thenReturn(null);
        when(rabbitAdmin.getQueueInfo(QUEUE_PS_DELETE_MESSAGES))
                .thenReturn(null);

        assertFalse(controller.isHandlingMessages());
        boolean hasOnlyErrorEvents = memoryAppender.getLoggedEvents().stream().allMatch(logEvent -> logEvent.getLevel().equals(Level.ERROR));
        assertTrue(hasOnlyErrorEvents);
        assertThat(memoryAppender.getLoggedEvents()).hasSize(3);
    }

    @Test()
    void testIsHandlingMessagesTrueWhenOneQueueIsNull() {
        QueueInformation updateQueueState = new QueueInformation(QUEUE_PS_UPDATE_MESSAGES, 20, 10);
        QueueInformation deleteQueueState = new QueueInformation(QUEUE_PS_DELETE_MESSAGES, 0, 0);
        MsgTimeChecker.getInstance().setMsgConsumptionTimestamp();

        when(rabbitAdmin.getQueueInfo(QUEUE_PS_CREATE_MESSAGES))
                .thenReturn(null);
        when(rabbitAdmin.getQueueInfo(QUEUE_PS_UPDATE_MESSAGES))
                .thenReturn(updateQueueState);
        when(rabbitAdmin.getQueueInfo(QUEUE_PS_DELETE_MESSAGES))
                .thenReturn(deleteQueueState);

        assertTrue(controller.isHandlingMessages());
        assertThat(memoryAppender.getLoggedEvents()).hasSize(2);
        assertThat(memoryAppender.getLoggedEvents().stream().filter(logEvent -> logEvent.getLevel().equals(Level.ERROR))).hasSize(1);
        assertThat(memoryAppender.getLoggedEvents().stream().filter(logEvent -> logEvent.getLevel().equals(Level.INFO))).hasSize(1);
    }


}
