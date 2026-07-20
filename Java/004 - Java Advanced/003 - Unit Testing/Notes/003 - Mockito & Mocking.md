# Mockito & Mocking

The class worth testing rarely stands alone: an `OrderService` calls a repository, a payment gateway, a mailer. Real ones make tests slow, flaky, and destructive (nobody wants test runs charging cards). A **mock** is a stand-in generated at runtime — an object of the right type whose every behavior the test scripts and whose every interaction the test can inspect. **Mockito** is the standard library for it, and it runs on machinery we now recognize: dynamically generated subclasses and interface implementations, reflection underneath.

---

## The Setup and the Shape

```groovy
testImplementation 'org.mockito:mockito-core:5.11.0'
testImplementation 'org.mockito:mockito-junit-jupiter:5.11.0'
```

*Two test-scoped dependencies: the core library and the JUnit 5 integration.*

The class under test, designed the way Unit 3 taught — dependencies as constructor-injected *interfaces*:

```java
public class OrderService {
    private final OrderRepository repo;
    private final PaymentGateway gateway;

    public OrderService(OrderRepository repo, PaymentGateway gateway) {
        this.repo = repo;
        this.gateway = gateway;
    }

    public Receipt checkout(String orderId) {
        Order order = repo.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        Receipt receipt = gateway.charge(order.getTotal());
        repo.save(order.markPaid());
        return receipt;
    }
}
```

*Testable by construction: both collaborators arrive through the constructor as interfaces — swappable for mocks.*

The test replaces both collaborators:

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository repo;                 // generated implementations —
    @Mock PaymentGateway gateway;               // every method returns defaults until stubbed

    @InjectMocks OrderService service;          // real class, mocks injected

    @Test
    void checkoutChargesAndMarksPaid() {
        Order order = new Order("ord-1", 99.0);
        when(repo.findById("ord-1")).thenReturn(Optional.of(order));       // STUB: script the world
        when(gateway.charge(99.0)).thenReturn(new Receipt("rcp-7"));

        Receipt receipt = service.checkout("ord-1");                        // ACT — pure logic, no I/O

        assertEquals("rcp-7", receipt.id());                                 // assert the outcome...
        verify(repo).save(argThat(o -> o.isPaid()));                        // ...and the crucial interaction
    }
}
```

*The full pattern: `@Mock` fabricates collaborators, `when/thenReturn` scripts them, the real logic runs, `verify` checks what it did to the world.*

---

## Stubbing and Verifying

**Stubbing** — `when(mock.method(args)).thenReturn(value)` — defines the world the test wants: this order exists, that charge succeeds. Variants: `thenThrow(new GatewayTimeoutException())` scripts failures (making error-path tests — retry? rollback? — trivial to arrange, often mocking's biggest payoff), `thenReturn(a, b)` sequences calls. Unstubbed methods return benign defaults (null, 0, false, empty collections/Optionals) — script only what the test needs.

**Verification** — `verify(mock).method(args)` — asserts an interaction happened. This is for *side effects that are the point*: the save, the sent email, the audit record — things no return value proves. Quantifiers when it matters: `verify(mailer, never()).send(any())`, `times(2)`, `verifyNoInteractions(gateway)`.

**Argument matchers** loosen or sharpen the matching: `any()`, `anyString()`, `eq(42)` (all-or-none per call — mixing a matcher with a bare literal is the classic Mockito error), and `argThat(predicate)` — a `Predicate`, naturally — for claims about the argument, as with the `isPaid()` check above. `ArgumentCaptor` goes further, capturing the argument for full-strength assertions afterward.

---

## The Discipline

Mockito makes over-mocking easy, and over-mocked suites are brittle suites — so the guardrails matter as much as the API:

- **Mock what's yours to isolate: boundaries.** Repositories, gateways, clocks, mailers — slow, external, nondeterministic things. Don't mock value objects (`Order` above is real — just construct it), collections, or the class under test.
- **Verify outcomes over interactions.** `verify` every internal call and the test re-specifies the implementation — refactor the method, watch correct code fail (the over-specification trap from last lesson, in mock form). Reserve `verify` for side effects with business meaning.
- **A wall of stubs is a design review.** Ten `when(...)` lines to test one method means the class has too many dependencies or the method too many jobs — the test suite functioning as the design feedback loop it secretly is.
- Terminology, for precision: a **stub** answers questions; a **mock** additionally records for verification; a **fake** is a real lightweight implementation (in-memory repository). Mockito objects serve as any of them.

Step back and the last three lessons form one argument: classes declare dependencies as interfaces and receive them from outside (constructor injection) → tests hand in mocks → production hands in real implementations. The only thing missing is something to do that production hand-in automatically, application-wide. That something is Spring — next topic.
