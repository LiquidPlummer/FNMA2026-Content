# JUnit Basics

A **unit test** is code that exercises other code and states, executably, what should happen: *given these inputs, this method returns that value; given this bad input, it throws.* **JUnit** (we use JUnit 5, a.k.a. Jupiter) is Java's standard test framework — the runner that discovers test methods by annotation (exactly the reflective pattern from last topic), executes them in isolation, and reports every broken promise. Professional Java treats tests as part of the code, not an accessory: the methods-as-contracts idea from Unit 2 finally gets its enforcement arm.

---

## The First Test

Tests live apart from production code — same package, different source root (`src/test/java` alongside `src/main/java` in the Maven/Gradle layout from Unit 1), with JUnit as a test-scoped dependency:

```groovy
dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.2'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

*The Gradle wiring — familiar from the build-script lessons; `gradle test` runs the suite.*

A test class is a plain class; a test is a method marked **`@Test`**:

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BankAccountTest {

    @Test
    void depositIncreasesBalance() {
        BankAccount acct = new BankAccount("Ada", 100.0);

        acct.deposit(50.0);

        assertEquals(150.0, acct.getBalance());
    }

    @Test
    void negativeDepositIsRejected() {
        BankAccount acct = new BankAccount("Ada", 100.0);

        assertThrows(IllegalArgumentException.class, () -> acct.deposit(-5.0));
    }
}
```

*Two executable specifications: normal behavior and the failure contract, each in its own method.*

A test **passes** by returning normally and **fails** by throwing — which is all an assertion is: `assertEquals` throws a descriptive error when actual ≠ expected. The runner (IDE test button, `gradle test`, CI) instantiates the class **freshly for each test method**, runs each `@Test`, and tallies. That per-test instance is a deliberate isolation guarantee: no state leaks between tests, and test order must never matter.

Names describe the *behavior*, not the method under test — `depositIncreasesBalance`, not `testDeposit1`. A failing test's name should read as the bug report.

---

## Lifecycle: Setup and Teardown

Repeated setup moves into lifecycle methods:

```java
class BankAccountTest {
    private BankAccount acct;

    @BeforeEach
    void freshAccount() {
        acct = new BankAccount("Ada", 100.0);     // runs before EVERY @Test
    }

    @AfterEach
    void cleanup() { /* release resources if any */ }

    @BeforeAll
    static void once() { /* expensive shared setup — static, use sparingly */ }

    @Test
    void withdrawalReducesBalance() {
        acct.withdraw(30.0);
        assertEquals(70.0, acct.getBalance());
    }
}
```

*`@BeforeEach`/`@AfterEach` bracket every test; `@BeforeAll`/`@AfterAll` run once per class.*

`@BeforeEach` keeps tests DRY without sacrificing isolation — every test still gets pristine objects. Shared *mutable* state in `@BeforeAll` is the classic way tests start corrupting each other; reserve it for immutable or read-only fixtures.

Rounding out the everyday annotation set: **`@DisplayName("deposit adds to balance")`** for human-readable reports, **`@Disabled("pending fix for #142")`** to skip with a reason (never silently), **`@Nested`** inner classes to group scenarios, and **`@ParameterizedTest`** — one test body, many inputs:

```java
@ParameterizedTest
@ValueSource(doubles = {0.01, 50.0, 99.99})
void validDepositsAreAccepted(double amount) {
    acct.deposit(amount);
    assertEquals(100.0 + amount, acct.getBalance());
}
```

*A parameterized test runs once per value — table-driven coverage without copy-paste.*

---

## What Makes a Test "Unit"

The unit in unit test is scope: **one class or method, isolated from real infrastructure** — no live databases, networks, or clocks. That isolation is what makes the suite *fast* (thousands of tests in seconds) and *deterministic* (failure means the code changed, not the Wi-Fi). It's also a design pressure with benefits: code that's hard to test in isolation usually has tangled dependencies — and the standard fix, depending on interfaces that tests can substitute (abstraction, again), is both the subject of lesson 3 (Mockito) and the philosophical core of Spring in the next topic.

The habit that makes all of it pay: run the suite constantly — before commits, in CI on every push — so a red bar means *the last small change*, the cheapest possible moment to learn something broke. First, though, a sharper look at the assertion vocabulary and the anatomy of a well-built test: next lesson.
