# Configuration & Profiles

An application's *code* shouldn't change between a laptop and production — but its database URL, credentials, feature flags, and pool sizes must. Spring's configuration system externalizes those values into property files and the environment, injects them where needed, and — via **profiles** — switches whole sets of beans and settings per environment. This is the last core-Spring piece: the container knowing not just *what* to wire, but *with which values, where*.

---

## Properties: Externalized Values

The convention-blessed home is **`application.properties`** (or `.yml`) on the classpath (`src/main/resources`):

```properties
server.port=8080
spring.datasource.url=jdbc:postgresql://localhost:5432/shop
shop.payment.gateway-url=https://api.stripe.com
shop.payment.timeout-seconds=10
shop.tax.rate=0.08
```

*Framework keys (`server.*`, `spring.*`) configure Spring itself; our own namespaced keys configure our beans.*

Crucially, this file is only the *base layer*. Spring resolves each key through a precedence chain — roughly: command-line arguments override **environment variables** override profile-specific files override `application.properties`. The same build ships everywhere; production overrides what it must (`SHOP_PAYMENT_GATEWAY_URL=...` — the relaxed-binding rule maps `UPPER_SNAKE` env vars onto dotted keys automatically). Secrets — real database passwords, API keys — belong in that outer layer, never committed in properties files: the same discipline as the Gradle credentials lesson in Unit 1.

Single values inject via **`@Value`**:

```java
@Service
public class TaxCalculator {
    private final double rate;

    public TaxCalculator(@Value("${shop.tax.rate}") double rate) {   // ${key} from the environment
        this.rate = rate;
    }
}
```

*`@Value("${...}")` pulls one property into a constructor parameter — with type conversion included.*

`@Value` scales badly past a couple of keys. Related settings belong in a **`@ConfigurationProperties`** class — one typed, validated object per config namespace:

```java
@ConfigurationProperties(prefix = "shop.payment")
public record PaymentProps(String gatewayUrl, int timeoutSeconds) {}   // keys bind by name

@Service
public class StripeGateway implements PaymentGateway {
    public StripeGateway(PaymentProps props) { ... }                   // injected like any bean
}
```

*Typed configuration: the `shop.payment.*` keys bind onto a record — refactorable, testable, documented by its own shape.*

---

## Profiles: Named Environments

A **profile** is a label for an environment — `dev`, `test`, `prod` — that activates matching configuration and beans. Profile-specific property files layer on top of the base:

```properties
# application-dev.properties — active only under the dev profile
spring.datasource.url=jdbc:postgresql://localhost:5432/shop_dev
logging.level.com.example.shop=DEBUG
shop.payment.gateway-url=https://sandbox.stripe.com
```

*`application-{profile}.properties`: overrides applied when that profile is active, on top of the base file.*

Beans themselves can be profile-gated with **`@Profile`** — entire implementations swapped per environment:

```java
@Component
@Profile("prod")
public class StripeGateway implements PaymentGateway { ... }      // real charges: prod only

@Component
@Profile("!prod")                                                  // everywhere EXCEPT prod
public class FakeGateway implements PaymentGateway { ... }         // logs instead of charging
```

*Two beans, one interface, mutually exclusive profiles — `OrderService` injects `PaymentGateway` and never knows which it got.*

This is the dependency-inversion payoff at environment scale: the alternative — `if (env.equals("prod"))` scattered through business logic — dies here. Activation comes from outside the code: `spring.profiles.active=dev` in a local file, `SPRING_PROFILES_ACTIVE=prod` as an environment variable in deployment, `@ActiveProfiles("test")` on test classes. Multiple profiles can be active at once; keep the set small and boring — `dev`/`test`/`prod` plus perhaps a feature toggle or two. Dozens of interacting profiles are a configuration combinatorics problem nobody wins.

---

## The Assembled Picture

Spring Core is now complete in outline: **scanning** finds components, **injection** wires them by type through constructors, **properties** supply environment values, **profiles** select per-environment variants — all resolved at startup, failing fast on contradictions. What we haven't explained is who *starts* all this — what turns `main` into a running, scanned, wired, property-resolved application, and why we never wrote a line of server-bootstrap code. That's Spring Boot and its auto-configuration, next.
