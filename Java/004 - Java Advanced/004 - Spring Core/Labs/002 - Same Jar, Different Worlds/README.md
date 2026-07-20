# Lab: Same Jar, Different Worlds — Configuration, Profiles & Auto-Configuration

The previous lab's greeting app returns — and grows environments. In this lab we pull hardcoded settings out into `application.properties` with `@Value`, group them into a `@ConfigurationProperties` record, override them per environment with profile-specific files, swap entire *beans* per environment with `@Profile`, watch the property precedence chain beat the file from the command line and the environment — and finish by making auto-configuration visible with `--debug` and the actuator. One jar, many worlds; not one line of business code changes between them.

---

## Prerequisites

| Software | Required Version |
|---|---|
| JDK (Temurin or any OpenJDK build) | 21 (LTS) |
| Apache Maven | 3.9.x |
| Spring Boot (via `pom.xml`, no install) | 4.1.x |

The project ships with the previous lab's app already wired ([GreetingService.java](src/main/java/com/curriculum/labs/GreetingService.java), [EmailSender.java](src/main/java/com/curriculum/labs/EmailSender.java), [ProfilesApplication.java](src/main/java/com/curriculum/labs/ProfilesApplication.java)) plus three starters we'll need: `web`, `actuator`, and `validation`. From this lab's folder:

```console
mvn spring-boot:run
```

`[email] Welcome aboard, Ada!` appears — and this time the app **keeps running**, because the `web` starter brought an embedded server (Ctrl+C stops it). That lingering server is not an accident; the actuator part needs it.

---

## Part 1 — Settings out of the source: @Value

`GreetingService` hardcodes two decisions that aren't code: the greeting text and the retry count. Changing either currently means recompiling — configuration's job is exactly to prevent that. First, declare them in [src/main/resources/application.properties](src/main/resources/application.properties):

```properties
greeter.greeting=Welcome aboard, %s!
greeter.retry-count=3
```

Then replace the two hardcoded fields in `GreetingService` with constructor-injected values:

```java
private final String greeting;
private final int retryCount;

public GreetingService(NotificationSender sender,
        @Value("${greeter.greeting}") String greeting,
        @Value("${greeter.retry-count}") int retryCount) {
    this.sender = sender;
    this.greeting = greeting;
    this.retryCount = retryCount;
}
```

*`@Value("${...}")`: the container resolves the placeholder from its property sources and injects it — note the `int` conversion happening for free.*

(Import `org.springframework.beans.factory.annotation.Value`.) Run: same output, but the greeting now lives in a text file. Change the property, run again — new greeting, no recompile of anything you authored. Also note the naming convention crossing the boundary: `retry-count` (kebab-case) in properties binds to `retryCount` (camelCase) in Java; Boot's *relaxed binding* translates.

---

## Part 2 — Related settings travel together: @ConfigurationProperties

`@Value` scales badly — five related settings would mean five annotations scattered across constructors, with no single place that *is* the config. The grouped form binds a prefix to a type. Create `GreeterProperties.java`:

```java
package com.curriculum.labs;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "greeter")
public record GreeterProperties(String greeting, int retryCount) { }
```

*A record as the shape of the `greeter.*` config block — immutable, constructor-bound, one glance tells you every knob.*

Enable scanning for it — add `@ConfigurationPropertiesScan` (import `org.springframework.boot.context.properties.ConfigurationPropertiesScan`) under `@SpringBootApplication` on `ProfilesApplication`. Then slim `GreetingService` back down: inject `GreeterProperties props` instead of the two `@Value` parameters, and use `props.greeting()` / `props.retryCount()` in the method. Run — identical behavior, better shape: the config has a *type*, appears in IDE autocomplete, and (exercise 2) can be validated at startup.

---

## Part 3 — Profile-specific files

Environments differ by *values* first. A file named `application-<profile>.properties` is loaded **on top of** the base file when that profile is active. Create `src/main/resources/application-dev.properties`:

```properties
greeter.greeting=Yo %s, welcome to the DEV sandbox
```

Run twice and compare:

```console
mvn spring-boot:run
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The first prints the base greeting; the second prints the dev one — and note the startup log line `The following 1 profile is active: "dev"`. The dev file only overrode one property: `retry-count` still comes from the base file. Profile files are *diffs against the base*, not replacements.

---

## Part 4 — Profiles swap beans, not just values

The sharper tool: entire beans that exist only in certain environments. Our `EmailSender` "really" emails people — nothing outside production should ever construct it. Two edits:

On `EmailSender`, add a profile gate (import `org.springframework.context.annotation.Profile`):

```java
@Component
@Profile("prod")
public class EmailSender implements NotificationSender {
```

And create its stand-in, `FakeSender.java` — present everywhere *except* prod:

```java
package com.curriculum.labs;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!prod")
public class FakeSender implements NotificationSender {

    @Override
    public void send(String message) {
        System.out.println("[fake]  would have sent: " + message);
    }
}
```

*`@Profile("prod")` and `@Profile("!prod")`: complementary conditions — exactly one sender exists in any given world.*

Run three ways and watch the wiring change with **zero code edits**:

```console
mvn spring-boot:run                                  # [fake]  would have sent: ...
mvn spring-boot:run -Dspring-boot.run.profiles=dev   # [fake]  ... (dev greeting, fake sender)
mvn spring-boot:run -Dspring-boot.run.profiles=prod  # [email] ... (the real one)
```

`GreetingService` still injects a single `NotificationSender` and never learned anything happened. This is the payoff of depending on the interface: the *container's contents* became environment-specific, not the code.

---

## Part 5 — The precedence chain, observed

`application.properties` is only the floor. Later sources override earlier ones; the two everyone uses are OS environment variables and command-line arguments. Watch each beat the file:

**Command line** (highest of the common sources):

```console
mvn spring-boot:run -Dspring-boot.run.arguments=--greeter.greeting="CLI says hi, %s"
```

**Environment variable** — note the naming translation, `greeter.greeting` → `GREETER_GREETING` (uppercase, dots to underscores):

```powershell
# PowerShell
$env:GREETER_GREETING = "The environment greets you, %s"
mvn spring-boot:run
Remove-Item Env:GREETER_GREETING
```

```bash
# bash / Git Bash
GREETER_GREETING="The environment greets you, %s" mvn spring-boot:run
```

Both override the file. For the tiebreak, set the env var **and** pass the CLI argument in one run: the CLI wins. The chain (the slice of it that matters daily) is: **command line > environment variables > profile-specific file > base `application.properties` > code defaults**. This is how the same jar is configured by a developer laptop, a CI pipeline, and a container orchestrator without any of them editing files inside it.

---

## Part 6 — Auto-configuration made visible

Time to look under the hood. Boot configured an embedded web server, JSON, actuator endpoints — none of which we wrote config for. That's **auto-configuration**: `@Bean` definitions that activate *conditionally* (class on the classpath? no user-defined bean of the type? property set?). The evidence is one flag away:

```console
mvn spring-boot:run -Dspring-boot.run.arguments=--debug
```

Scroll to the **CONDITIONS EVALUATION REPORT**. Two sections matter:

- **Positive matches** — find `WebMvcAutoConfiguration`: matched because `@ConditionalOnClass` found Spring MVC's classes (the `web` starter put them there) and no bean of ours already claimed the job.
- **Negative matches** — pick any entry and read *why* it didn't apply; you'll find lines like `did not find required class ...`. That's an entire feature declining to configure itself because its trigger library isn't on the classpath.

Write down one of each — the scavenger hunt in exercise 3 builds on this skill. The mental model to leave with: **starters put classes on the classpath; auto-configuration reacts to the classpath; properties and your own beans steer or veto it.** Boot isn't magic — it's a very long, very disciplined list of `if` statements you can read.

---

## Exercises

1. **The test world.** The generated [src/test/java/com/curriculum/labs/ProfilesApplicationTests.java](src/test/java/com/curriculum/labs/ProfilesApplicationTests.java) runs `@SpringBootTest` — the whole container, in a test. Give tests their own world: an `application-test.properties` with a distinctive greeting, `@ActiveProfiles("test")` on the test class, and a test that injects `GreeterProperties` (`@Autowired` field injection is acceptable in tests) and asserts the test-world values are bound. Then answer: which sender bean exists during this test run, and which annotation in this lab guarantees it?

2. **Fail at startup, not at 3 a.m.** Add validation to the record: `@Validated` on the class (import `org.springframework.validation.annotation.Validated`) and `@Positive` (import `jakarta.validation.constraints.Positive`) on `retryCount`. Set `greeter.retry-count=-1` in the base properties file and start the app. Read the failure top to bottom — note it names the property, the file, the rule, and never constructs a single bean. Then answer in a comment: what would the failure look like *without* validation — where and when would `-1` retries first misbehave? Revert to `3` when done.

3. **Actuator scavenger hunt.** Expose the needed endpoints by adding to `application.properties`: `management.endpoints.web.exposure.include=health,beans,env`. Start the app (leave it running) and answer all three **using only your browser or curl** against `http://localhost:8080` — no reading source:
   a. From `/actuator/beans`: what is the exact bean *name* of the active `NotificationSender`, and what happens to that entry when you restart under the `prod` profile?
   b. From `/actuator/env`: run with the CLI greeting override from Part 5 — in which named PropertySource does `/actuator/env` show your override, and where does the *file's* value still appear on the same page?
   c. From `/actuator/beans`: find `greeterProperties` and report which configuration class registered it — then explain how that connects to the annotation you added in Part 2.
