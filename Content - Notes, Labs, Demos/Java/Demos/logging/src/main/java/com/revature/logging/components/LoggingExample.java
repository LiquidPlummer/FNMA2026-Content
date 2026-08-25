package com.revature.logging.components;

import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class LoggingExample {
    private static final Logger log = LoggerFactory.getLogger(LoggingExample.class);


    public void logTrace(String message) {
        log.trace(message);
    }

    public void logDebug(String message) {
        log.debug(message);
    }

    public void logInfo(String message) {
        log.info(message);
    }

    public void logWarn(String message) {
        log.warn(message);
    }

    public void logError(String message) {
        log.error(message);
    }

    /*
    ERROR
    WARN
    INFO
    DEBUG
    TRACE

     */


}
