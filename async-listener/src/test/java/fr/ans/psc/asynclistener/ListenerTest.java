package fr.ans.psc.asynclistener;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import fr.ans.psc.asynclistener.model.AmarUserAdapter;
import fr.ans.psc.model.Ps;
import fr.ans.psc.rabbitmq.conf.PscRabbitMqConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
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

@SpringBootTest
@ContextConfiguration(classes = PsclAsyncListenerApplication.class)
class ListenerTest {

    // TODO this test must be run with a rmq server started on http://localhost:15672

    @Autowired
    Listener listener;

    @Autowired
    MessageProducer producer;

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

//    @Test
//    void test() throws Exception {
//        httpMockServer.stubFor(get("/api/ps/1").willReturn(aResponse()
//                .withBodyFile("ps1.json")
//                .withHeader("Content-Type", "application/json")
//                .withStatus(200)));
//        httpMockServer.stubFor(put("/api/ps").willReturn(aResponse()
//                .withStatus(200)));
//        httpMockServer.stubFor(put("/api/lura/ing/rass/users?nationalId=1").willReturn(aResponse()
//                .withStatus(200)));
//
//        ContactInfosWithNationalId contactInfos = new ContactInfosWithNationalId();
//        contactInfos.setEmail("test@test.org");
//        contactInfos.setNationalId("1");
//        contactInfos.setPhone("1234567890");
//        Gson json = new Gson();
//        producer.sendContactMessage(json.toJson(contactInfos));
//        Thread.sleep(50000L);
//        ForkJoinPool.commonPool().awaitQuiescence(10, TimeUnit.SECONDS);
//
//    }

    // test :
// préparer les stubs API : code psc api : 200 (cas passants), 410 (pas de create AMAR), 404 (pas de delete AMAR)
//	préparer les stubs AMAR
//	envoyer les messages dans les différentes queues

    @Test
    void testCreatePsOk() throws InterruptedException {
        httpMockServer.stubFor(get("/api/v2/ps/1").willReturn(aResponse()
                .withBodyFile("ps2.json")
                .withHeader("Content-Type", "application/json")
                .withStatus(200)));

        httpMockServer.stubFor(put("/api/lura/ing/rass/user?nationalId=1").willReturn(aResponse()
                .withStatus(200)));

        Gson gson = new Gson();
        Ps queuedPs = getTestingPs();
        producer.sendPsMessage(PS_CREATE_MESSAGES_QUEUE_ROUTING_KEY, gson.toJson(queuedPs, Ps.class));
        Thread.sleep(20000L);

        //TODO check if amar has been called with amarUser in body
    }

    @Test
    void testCreatePsFailedBecauseNotExist() throws InterruptedException {
        httpMockServer.stubFor(get("/api/v2/ps/1").willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(410)));

        Gson gson = new Gson();
        Ps queuedPs = getTestingPs();
        producer.sendPsMessage(PS_CREATE_MESSAGES_QUEUE_ROUTING_KEY, gson.toJson(queuedPs, Ps.class));
        Thread.sleep(20000L);

        //TODO check if amar has not been called with amarUser in body and message has been sent in dlq
    }

    @Test
    void testCreatePsFailedBecauseAmarTimeOut() throws InterruptedException {
        httpMockServer.stubFor(get("/api/v2/ps/1").willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBodyFile("ps2.json")
                .withStatus(200)));

        httpMockServer.stubFor(put("/api/lura/ing/rass/user?nationalId=1").willReturn(aResponse()
                .withStatus(500)));

        Gson gson = new Gson();
        Ps queuedPs = getTestingPs();
        producer.sendPsMessage(PS_CREATE_MESSAGES_QUEUE_ROUTING_KEY, gson.toJson(queuedPs, Ps.class));
        Thread.sleep(20000L);

        //TODO check if amar has not been called with amarUser in body and message has been sent in dlq
    }

    @Test
    void testDeletePsOk() throws InterruptedException {
        httpMockServer.stubFor(get("/api/v2/ps/1").willReturn(aResponse()
                .withBodyFile("ps2.json")
                .withHeader("Content-Type", "application/json")
                .withStatus(200)));

        httpMockServer.stubFor(delete("/api/lura/ing/rass/user?nationalId=1").willReturn(aResponse()
                .withStatus(200)));

        Gson gson = new Gson();
        Ps queuedPs = getTestingPs();
        AmarUserAdapter amarUser = new AmarUserAdapter(queuedPs);
        producer.sendPsMessage(PS_DELETE_MESSAGES_QUEUE_ROUTING_KEY, gson.toJson(queuedPs, Ps.class));
        Thread.sleep(20000L);

        //TODO check if amar has been called
    }

    @Test
    void testDeletePsFailed() throws InterruptedException {
        httpMockServer.stubFor(get("/api/v2/ps/1").willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(410)));

        Gson gson = new Gson();
        Ps queuedPs = getTestingPs();
        AmarUserAdapter amarUser = new AmarUserAdapter(queuedPs);
        producer.sendPsMessage(PS_DELETE_MESSAGES_QUEUE_ROUTING_KEY, gson.toJson(queuedPs, Ps.class));
        Thread.sleep(20000L);

        //TODO check if amar has not been called and message has been sent in dlq
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
