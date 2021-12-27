package fr.ans.psc.asynclistener;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.matching.AnythingPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.google.gson.Gson;

import fr.ans.psc.asynclistener.consumer.Listener;
import fr.ans.psc.asynclistener.model.ContactInfosWithNationalId;

@SpringBootTest
class ListenerTest {

	// TODO this test must be run with a rmq server started on http://localhost:15672
	
	@Autowired
	Listener listener;

	@Autowired
	MessageProducer producer;

	/** The http mock server. */
	@RegisterExtension
	static WireMockExtension httpMockServer = WireMockExtension.newInstance()
			.options(wireMockConfig().dynamicPort().usingFilesUnderClasspath("wiremock")).build();

	@DynamicPropertySource
	static void registerPgProperties(DynamicPropertyRegistry propertiesRegistry) {
		propertiesRegistry.add("in.api.url", () -> httpMockServer.baseUrl() + "/api/lura/ing/rass");
		propertiesRegistry.add("files.directory",
				() -> Thread.currentThread().getContextClassLoader().getResource("work").getPath());
		propertiesRegistry.add("psc.api.url", () -> httpMockServer.baseUrl() + "/api");
	}

	@Test
	void test() throws Exception {
		httpMockServer.stubFor(get("/api/ps/1").willReturn(aResponse()
				.withBodyFile("ps1.json")
	    		.withHeader("Content-Type", "application/json")
				.withStatus(200)));
		httpMockServer.stubFor(put("/api/ps").willReturn(aResponse()
				.withStatus(200)));
		httpMockServer.stubFor(put("/api/lura/ing/rass/users?nationalId=1").willReturn(aResponse()
				.withStatus(200)));
		
		ContactInfosWithNationalId contactInfos = new ContactInfosWithNationalId();
		contactInfos.setEmail("test@test.org");
		contactInfos.setNationalId("1");
		contactInfos.setPhone("1234567890");
		Gson json = new Gson();
		producer.sendContactMessage(json.toJson(contactInfos));
		Thread.sleep(50000L);
		ForkJoinPool.commonPool().awaitQuiescence(10, TimeUnit.SECONDS);

	}

}
