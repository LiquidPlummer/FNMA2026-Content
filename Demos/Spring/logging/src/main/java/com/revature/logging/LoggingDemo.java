package com.revature.logging;

import com.revature.logging.components.LoggingExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

//TODO:
//Revisit Inversion of Control as a concept
//Component scanning - stereotypes, annotations
//springbootapplication settings & implicit annotations
//autowiring - constructor, setter, why?, and the big no-no
//bean scopes - SINGLETON is EAGER by default - "earliest available opportunity"
//             PROTOTYPE is LAZY - created at the last possible moment - "ad hoc"
//             Prototype beans cannot be EAGER because we cannot know how many we need, 0-many
//The basics of the bean lifecycle
//hooking into the bean lifecycle with interfaces & annotations
//actuator and devtools


@SpringBootApplication(scanBasePackages = {
        "com.revature.logging.controllers",
        "com.revature.logging.repositories",
        "com.revature.logging.services",
        "com.revature.logging.components"})
public class LoggingDemo {
    private static final Logger log = LoggerFactory.getLogger(LoggingDemo.class);


    public static void main(String[] args) {
		ApplicationContext applicationContext = SpringApplication.run(LoggingDemo.class, args);
        LoggingExample loggingExample = applicationContext.getBean(LoggingExample.class);

        log.warn("Bean: {}", loggingExample);


        loggingExample.logError("Error level");
        loggingExample.logWarn("logWarn level");
        loggingExample.logDebug("logDebug level");
        loggingExample.logInfo("logInfo level");
        loggingExample.logTrace("logTrace level");

	}



}

