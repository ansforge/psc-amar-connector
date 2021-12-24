package fr.ans.psc.asynclistener;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.gson.Gson;

import fr.ans.psc.asynclistener.consumer.Listener;
import fr.ans.psc.asynclistener.model.ContactInfosWithNationalId;

@SpringBootTest
class ListenerTest {

	@Autowired
	Listener listener;
	
	@Autowired
	MessageProducer producer;
	
	@Test
	void test() throws Exception {
	ContactInfosWithNationalId contactInfos = new ContactInfosWithNationalId();
	contactInfos.setEmail("test@test.org");
	contactInfos.setNationalId("1");
	contactInfos.setPhone("1234567890");
	Gson json = new Gson();
	producer.sendContactMessage(json.toJson(contactInfos));
	}

	
	
}
