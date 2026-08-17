package com.revature.SPR_GCE_ANNO_CONFIG;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

//Spring Configuration methods:
// XML - we write XML files that describe the bean objects
//    Somewhere we need an XML file that can describe beans
// Java config - we have factory classes marked with the @Bean annotation that produce bean objects
// Annotation and Component Scanning - Spring uses reflection to scan and learn about our bean classes,
// which we mark with annotations to configure the application.




@SpringBootApplication(scanBasePackages = {
		"com.revature.SPR_GCE_ANNO_CONFIG.beans",

})
public class Main {
	public static void main(String[] args) {
		ApplicationContext ac = SpringApplication.run(Main.class, args);
	}
}
