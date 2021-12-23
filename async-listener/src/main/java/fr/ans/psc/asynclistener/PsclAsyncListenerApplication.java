/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.asynclistener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * The Class PsclAsyncListenerApplication.
 */
@SpringBootApplication
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
