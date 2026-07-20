package com.curriculum.labs;

/**
 * Pretend this class arrived in a third-party jar: we can read it, but we
 * CANNOT edit it — in particular, we can't put a Spring annotation on it.
 * That constraint is the whole point of the @Bean part of the walkthrough.
 */
public final class TemplateEngine {

    private final String prefix;

    public TemplateEngine(String prefix) {
        this.prefix = prefix;
    }

    public String render(String message) {
        return prefix + message;
    }
}
