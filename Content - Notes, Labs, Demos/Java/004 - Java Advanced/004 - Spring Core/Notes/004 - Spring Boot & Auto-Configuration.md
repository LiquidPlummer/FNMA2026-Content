# Spring Boot & Auto-Configuration

Classic Spring required assembling the framework itself — servlet containers, dispatchers, transaction managers, XML or Java config for all of it — before writing a line of business code. **Spring Boot** is the layer that deleted that phase: opinionated defaults, dependency bundles, and **auto-configuration** that infers the infrastructure an application needs from what's on its classpath. It's why a working web service today fits in one file.

---

## The Anatomy of a Boot Application

```java
@SpringBootApplication
public class ShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }
}
```

*The entire bootstrap: one annotation, one line of `main` — everything else is inference.*

`SpringApplication.run` builds the `ApplicationContext`, and **`@SpringBootApplication`** is three annotations in one:

- **`@ComponentScan`** — scan for stereotypes *from this class's package down* (why the main class sits at the root package; components outside it silently vanish — the classic "no candidate bean" mystery from two lessons ago).
- **`@Configuration`** — the class may declare `@Bean` methods.
- **`@EnableAutoConfiguration`** — the Boot magic switch, explained below.

With a web dependency present, running `main` starts an embedded Tomcat on port 8080 — the server lives *inside* the application (the modern deployment: one runnable JAR, `java -jar shop.jar`, container-friendly), not the application inside a server.

---

## Starters: Dependencies in Bundles

Boot packages coherent dependency sets as **starters** — one coordinate per capability:

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'        // MVC + Jackson + Tomcat
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'   // JPA + Hibernate
    runtimeOnly 'org.postgresql:postgresql'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'   // JUnit 5 + Mockito + more
}
```

*Starters: curated, version-aligned bundles — the Gradle dependency lessons from Unit 1, with the version matrix outsourced.*

The Boot Gradle plugin pins compatible versions across the whole set (no hand-matching Jackson to Spring to Hibernate), and `spring-boot-starter-test` arrives with the entire toolkit of the previous topic preinstalled.

---

## Auto-Configuration: Convention Inferred from the Classpath

The distinctive idea. At startup, Boot evaluates hundreds of **auto-configuration classes**, each a bundle of `@Bean` definitions guarded by conditions:

- *`spring-boot-starter-web` on the classpath?* → configure embedded Tomcat, request routing, JSON message conversion.
- *JPA plus a PostgreSQL driver present, and `spring.datasource.url` set?* → connection pool, `EntityManagerFactory`, transaction manager.
- In short: **presence of a dependency is treated as intent to use it**, wired with sensible defaults, tuned via properties (`server.port=9090`) rather than code.

The crucial rule making this safe is *conditionality* — mechanically, annotations like `@ConditionalOnMissingBean` guard every default:

```java
// Simplified from Boot's own source
@Bean
@ConditionalOnMissingBean(ObjectMapper.class)      // only if WE haven't defined one
public ObjectMapper jacksonObjectMapper() { ... }
```

*Auto-configuration always yields: define the bean ourselves (as in the Beans lesson) and Boot's version stands down.*

So the experience degrades gracefully from zero-config to full control: accept defaults → adjust properties → replace individual beans — never fight the framework, just out-declare it. When wiring surprises anyway, the diagnostics are built in: `--debug` prints the **condition evaluation report** (every auto-config, applied or skipped, with reasons), and the **Actuator** starter exposes runtime views (`/actuator/beans`, `/actuator/env`, `/actuator/health` — the last one being what deployment platforms probe).

---

## The Trade, Stated Honestly

Boot trades explicitness for velocity. The costs: behavior that materializes from the classpath can surprise (adding a dependency *changes the application*), defaults chosen for the common case fit the uncommon one poorly, and debugging requires knowing the conditional model above — "magic" is just the name for machinery we haven't read yet. The mitigation is exactly the arc of this unit: annotations + reflection + conditions is machinery we *have* read. Treat auto-configuration as a colleague's code — inspectable, overridable, occasionally wrong — rather than sorcery, and Boot is a pure productivity win.

Spring Core is assembled: container, beans, injection, configuration, and the bootstrap that turns it on. The next two topics put beans to work on the two jobs nearly every service has — persisting data (Spring Data) and serving HTTP (Spring Web).
