package com.revature.SPR_GCE_ANNO_CONFIG;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication(scanBasePackages = {
		"com.revature.SPR_GCE_ANNO_CONFIG.beans",

})
public class Main {
	public static void main(String[] args) {
		ApplicationContext ac = SpringApplication.run(Main.class, args);
	}
}
