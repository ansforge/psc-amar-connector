/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.asynclistener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * The Class PsclAsyncListenerApplication.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"fr.ans.psc.asynclistener", "fr.ans.psc.rabbitmq.conf"})
public class PsclAsyncListenerApplication {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		ConfigurableApplicationContext applicationContext = SpringApplication.run(PsclAsyncListenerApplication.class, args);
		applicationContext.start();
	}
}
