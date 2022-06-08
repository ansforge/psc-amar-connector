package fr.ans.psc.asynclistener;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import fr.ans.psc.asynclistener.model.AmarUserAdapter;
import fr.ans.psc.asynclistener.utils.MemoryAppender;
import fr.ans.psc.model.Ps;
import fr.ans.psc.rabbitmq.conf.PscRabbitMqConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.matching.AnythingPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.google.gson.Gson;

import fr.ans.psc.asynclistener.consumer.Listener;
import fr.ans.psc.asynclistener.model.ContactInfosWithNationalId;
import static fr.ans.psc.rabbitmq.conf.PscRabbitMqConfiguration.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = PsclAsyncListenerApplication.class)
@ActiveProfiles("test")
class ListenerTest {

    // TODO this test must be run with a rmq server started on http://localhost:15672

    @Autowired
    Listener listener;

    @Autowired
    MessageProducer producer;

    private MemoryAppender memoryAppender;

    /**
     * The http mock server.
     */
    @RegisterExtension
    static WireMockExtension httpMockServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().usingFilesUnderClasspath("wiremock")).build();

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry propertiesRegistry) {
        propertiesRegistry.add("in.api.url", () -> httpMockServer.baseUrl() + "/api/lura/ing/rass");
        propertiesRegistry.add("files.directory",
                () -> Thread.currentThread().getContextClassLoader().getResource("work").getPath());
        propertiesRegistry.add("psc.api.url", () -> httpMockServer.baseUrl() + "/api");
        propertiesRegistry.add("in.amar.url", () -> httpMockServer.baseUrl() + "/api/lura");
        propertiesRegistry.add("amar.production.ready", () -> true);
    }

    @BeforeEach
    public void setup() {
        // LOG APPENDER
        Logger logger = (Logger) LoggerFactory.getLogger(Listener.class);
        memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.setLevel(Level.DEBUG);
        logger.addAppender(memoryAppender);
        memoryAppender.start();
    }

    @Test
    void testCreatePsOk() throws InterruptedException {
        httpMockServer.stubFor(get("/api/v2/ps/1?include=otherIds").willReturn(aResponse()
                .withBodyFile("ps2.json")
                .withHeader("Content-Type", "application/json")
                .withStatus(200)));

        httpMockServer.stubFor(put("/api/lura/ing/rass/user?nationalId=1").willReturn(aResponse()
                .withStatus(200)));

        Gson gson = new Gson();
        Ps queuedPs = getTestingPs();
        producer.sendPsMessage(PS_CREATE_MESSAGES_QUEUE_ROUTING_KEY, gson.toJson(queuedPs, Ps.class));
        Thread.sleep(2000);

        assertThat(memoryAppender.contains(
                "PS 1 successfully stored in AMAR, routing key was PS_CREATE_MESSAGES_QUEUE_ROUTING_KEY", Level.DEBUG))
                .isTrue();

    }

    @Test
    void testCreatePsFailedBecauseNotExist() throws InterruptedException {
        httpMockServer.stubFor(get("/api/v2/ps/1?include=otherIds").willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(410)));

        Gson gson = new Gson();
        Ps queuedPs = getTestingPs();
        producer.sendPsMessage(PS_CREATE_MESSAGES_QUEUE_ROUTING_KEY, gson.toJson(queuedPs, Ps.class));
        Thread.sleep(2000L);

        assertThat(memoryAppender.contains(
                "Ps 1 does not exist in sec-psc database, will not be sent to AMAR", Level.INFO))
                .isTrue();
    }

    @Test
    void testCreatePsFailedBecauseAmarTimeOut() throws InterruptedException {
        httpMockServer.stubFor(get("/api/v2/ps/1?include=otherIds").willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBodyFile("ps2.json")
                .withStatus(200)));

        httpMockServer.stubFor(put("/api/lura/ing/rass/user?nationalId=1").willReturn(aResponse()
                .withStatus(500)));

        Gson gson = new Gson();
        Ps queuedPs = getTestingPs();
        producer.sendPsMessage(PS_CREATE_MESSAGES_QUEUE_ROUTING_KEY, gson.toJson(queuedPs, Ps.class));
        Thread.sleep(2000L);

        assertThat(memoryAppender.contains(
                "PS 1 not stored in AMAR, moved to dead letter queue", Level.WARN))
                .isTrue();
    }

    @Test
    void testDeletePsOk() throws InterruptedException {
        httpMockServer.stubFor(get("/api/v2/ps/1?include=otherIds").willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(410)));

        httpMockServer.stubFor(delete("/api/lura/ing/rass/user?nationalId=1").willReturn(aResponse()
                .withStatus(200)));

        Gson gson = new Gson();
        Ps queuedPs = getTestingPs();
        AmarUserAdapter amarUser = new AmarUserAdapter(queuedPs);
        producer.sendPsMessage(PS_DELETE_MESSAGES_QUEUE_ROUTING_KEY, gson.toJson(queuedPs, Ps.class));
        Thread.sleep(2000L);

        assertThat(memoryAppender.contains(
                "PS 1 successfully deleted in AMAR", Level.DEBUG))
                .isTrue();
    }

    @Test
    void testDeletePsFailedStillExists() throws InterruptedException {
        httpMockServer.stubFor(get("/api/v2/ps/1?include=otherIds").willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBodyFile("ps2.json")
                .withStatus(200)));

        Gson gson = new Gson();
        Ps queuedPs = getTestingPs();
        producer.sendPsMessage(PS_DELETE_MESSAGES_QUEUE_ROUTING_KEY, gson.toJson(queuedPs, Ps.class));
        Thread.sleep(2000L);

        assertThat(memoryAppender.contains(
                "Ps 1 still exists in sec-psc database, will not be sent to AMAR", Level.INFO))
                .isTrue();
    }

    @Test
    void testDeletePsFailedStillBecauseOfAmarTimeOut() throws InterruptedException {
        httpMockServer.stubFor(get("/api/v2/ps/1?include=otherIds").willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(410)));

        httpMockServer.stubFor(delete("/api/lura/ing/rass/user?nationalId=1").willReturn(aResponse()
                .withStatus(500)));

        Gson gson = new Gson();
        Ps queuedPs = getTestingPs();
        producer.sendPsMessage(PS_DELETE_MESSAGES_QUEUE_ROUTING_KEY, gson.toJson(queuedPs, Ps.class));
        Thread.sleep(2000L);

        assertThat(memoryAppender.contains(
                "PS 1 not deleted in AMAR, moved to dead letter queue", Level.WARN))
                .isTrue();
    }

    private Ps getTestingPs() {
        Gson gson = new Gson();
        String psJson = "{\"idType\":\"8\",\"id\":\"00000000001\"," +
                "\"nationalId\":\"1\",\"lastName\":\"DUPONT\",\"firstName\":\"JIMMY'MIKE'ERICK-RIEGEL\",\"dateOfBirth\":\"17/12/1983\"," +
                "\"birthAddressCode\":\"57463\",\"birthCountryCode\":\"99000\",\"birthAddress\":\"METZ\",\"genderCode\":\"M\"," +
                "\"phone\":\"0601020304\",\"email\":\"toto57@hotmail.fr\",\"salutationCode\":\"MME\",\"professions\":[{\"exProId\":\"50C\"," +
                "\"code\":\"50\",\"categoryCode\":\"C\",\"salutationCode\":\"M\",\"lastName\":\"DUPONT\",\"firstName\":\"JIMMY\"," +
                "\"expertises\":[{\"expertiseId\":\"SSM69\",\"typeCode\":\"S\",\"code\":\"SM69\"}],\"workSituations\":[{\"situId\":\"SSA04\"," +
                "\"modeCode\":\"S\",\"activitySectorCode\":\"SA04\",\"pharmacistTableSectionCode\":\"AC36\",\"roleCode\":\"12\"," +
                "\"registrationAuthority\":\"ARS/ARS/ARS\", \"structure\":{\"siteSIRET\":\"125 137 196 15574\",\"siteSIREN\":\"125 137 196\"," +
                "\"siteFINESS\":null,\"legalEstablishmentFINESS\":null,\"structureTechnicalId\":\"1\"," +
                "\"legalCommercialName\":\"Structure One\",\"publicCommercialName\":\"Structure One\",\"recipientAdditionalInfo\":\"info +\"," +
                "\"geoLocationAdditionalInfo\":\"geoloc info +\",\"streetNumber\":\"1\",\"streetNumberRepetitionIndex\":\"bis\"," +
                "\"streetCategoryCode\":\"rue\",\"streetLabel\":\"Zorro\",\"distributionMention\":\"c/o Bernardo\",\"cedexOffice\":\"75117\"," +
                "\"postalCode\":\"75017\",\"communeCode\":\"75\",\"countryCode\":\"FR\",\"phone\":\"0123456789\",\"phone2\":\"0623456789\"," +
                "\"fax\":\"0198765432\",\"email\":\"structure@one.fr\",\"departmentCode\":\"99\",\"oldStructureId\":\"101\"," +
                "\"registrationAuthority\":\"CIA\"}}]}],\"otherIds\":[\"1\", \"ALT-ID\"]}";

        return gson.fromJson(psJson, Ps.class);
    }
}
