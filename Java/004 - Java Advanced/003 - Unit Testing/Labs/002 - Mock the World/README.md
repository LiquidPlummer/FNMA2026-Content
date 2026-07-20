# Lab: Mock the World — Mockito & Mocking

In this lab we test an `OrderService` whose collaborators — an `OrderRepository` and a `PaymentGateway` — are exactly the things unit tests must not touch for real. We'll fabricate them with `@Mock`, script them with `when/thenReturn` and `thenThrow`, and inspect what the service did to them with `verify`, `never()`, `argThat`, and `ArgumentCaptor`. Working fakes are provided so the app *runs* — and the discipline of the lab is that your tests won't use them.

---

## Prerequisites

| Software | Required Version |
|---|---|
| JDK (Temurin or any OpenJDK build) | 21 (LTS) |
| Apache Maven | 3.9.x |
| JUnit (via `pom.xml`) | 5.11.x |
| Mockito (via `pom.xml`) | 5.14.x |

Check [pom.xml](pom.xml): alongside JUnit you'll find `mockito-core` and `mockito-junit-jupiter`, both test-scoped. From this lab's folder:

```console
mvn -q compile exec:java     # runs Main — the service working end to end with fakes
mvn test                     # runs the suite (one passing review test, one skipped, none of yours yet)
```

The cast, in [src/main/java/com/curriculum/labs/](src/main/java/com/curriculum/labs/):

- **`OrderService`** — the class under test. Read `checkout` now; it's fifteen lines: find the order, refuse non-OPEN orders, charge the gateway, mark PAID, save, return a `Receipt`; a gateway timeout becomes `PaymentFailedException`.
- **`OrderRepository`**, **`PaymentGateway`** — the two ports, as interfaces. Constructor-injected. This is why the service is testable at all.
- **`Order`**, **`PaymentResult`**, **`Receipt`**, three exceptions — the vocabulary.
- **`InMemoryOrderRepository`**, **`FakePaymentGateway`**, **`Main`** — the fakes and a runnable demo. Here for orientation, off-limits to your tests. (Why? A fake answers however *it* decides; a test needs each collaborator to answer however *the test* decides — including deciding to fail. That's the difference between a fake and a mock.)

You'll write tests in [src/test/java/com/curriculum/labs/OrderServiceTest.java](src/test/java/com/curriculum/labs/OrderServiceTest.java). Run `mvn test` after every part.

---

## Part 1 — The rig: @Mock and @InjectMocks

Mockito integrates with JUnit through an extension, and declares mocks with annotations. At the Part 1 marker in `OrderServiceTest`, we'll set up the whole rig — this goes *on and inside* the class:

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository repository;

    @Mock
    PaymentGateway gateway;

    @InjectMocks
    OrderService service;
```

*Two fabricated collaborators and the real class under test, wired together before each test.*

Imports: `org.junit.jupiter.api.extension.ExtendWith`, `org.mockito.Mock`, `org.mockito.InjectMocks`, `org.mockito.junit.jupiter.MockitoExtension`.

What's actually happening: for each test, the extension generates an implementation of each `@Mock` interface at runtime (dynamic subclassing — reflection, last topic, earning its living), then constructs the `@InjectMocks` target by matching those mocks to its constructor parameters. `service` is a completely real `OrderService`; only its world is synthetic. Until scripted, every mock method returns benign defaults — `null`, `0`, `false`, empty `Optional` — which will matter sooner than you think.

---

## Part 2 — The happy path: when/thenReturn

A test states the world, then lets the logic run. Add:

```java
@Test
void checkoutChargesAndReturnsAReceipt() {
    // Arrange — script the world
    Order order = new Order("A-1", "cust-42", 120.00, "OPEN");
    when(repository.findById("A-1")).thenReturn(Optional.of(order));
    when(gateway.charge("cust-42", 120.00)).thenReturn(new PaymentResult("TX-9"));

    // Act — real logic, zero I/O
    Receipt receipt = service.checkout("A-1");

    // Assert — the outcome
    assertAll("receipt",
        () -> assertEquals("A-1", receipt.orderId()),
        () -> assertEquals("TX-9", receipt.transactionId()),
        () -> assertEquals(120.00, receipt.amountCharged(), 0.001));
}
```

*`when(...).thenReturn(...)`: this order exists, that charge succeeds — the test writes the script, the service acts it out.*

Static imports do the heavy lifting here: `import static org.mockito.Mockito.*;` and `import static org.junit.jupiter.api.Assertions.*;` (plus `java.util.Optional`). Two things worth savoring. `findById` returns `Optional.of(order)` — the Optional discipline from the Intermediate unit is now the *stubbing* vocabulary; to script a missing order you'd return `Optional.empty()`, not `null`. And notice `Order` itself is **not** mocked — it's a value object; we just built one. Mock the boundaries, construct the values.

---

## Part 3 — Verifying the side effect that matters: argThat

The receipt proves the return value; it doesn't prove the order was *persisted as paid* — a side effect no return value shows. That's what `verify` is for. Append to the same test (after the `assertAll`):

```java
    verify(repository).save(argThat(saved ->
            "PAID".equals(saved.getStatus()) && "TX-9".equals(saved.getTransactionId())));
```

*`verify(mock).method(...)`: assert the interaction happened — with `argThat` making a claim about what was passed.*

(`argThat` comes from `org.mockito.ArgumentMatchers`, also covered by `Mockito.*`.) The predicate is the point: we don't check *which object reference* was saved — we check that whatever was saved *was in the right state*. Save-the-paid-order is business behavior; that's the kind of interaction worth verifying. What we deliberately do **not** verify: that `findById` was called. The service can't work without reading — verifying reads re-describes the implementation and buys nothing. Outcomes over interactions; hold that line.

---

## Part 4 — The error path: thenThrow

Error handling is where mocking pays best — scripting a gateway timeout for real would take a network and bad luck; with a mock, it's one line:

```java
@Test
void gatewayTimeoutBecomesPaymentFailedAndNothingIsSaved() {
    Order order = new Order("A-1", "cust-42", 120.00, "OPEN");
    when(repository.findById("A-1")).thenReturn(Optional.of(order));
    when(gateway.charge(anyString(), anyDouble())).thenThrow(new GatewayTimeoutException("no answer"));

    PaymentFailedException ex = assertThrows(
            PaymentFailedException.class,
            () -> service.checkout("A-1"));

    assertEquals(GatewayTimeoutException.class, ex.getCause().getClass());
    verify(repository, never()).save(any());
}
```

*`thenThrow` scripts the failure; the assertions pin down the service's translation contract — and that a half-charged order never hits the database.*

Read the three claims: the service converts the gateway's exception into its own (`PaymentFailedException`), preserves the original as the cause (exception chaining, from the exceptions lessons), and — the sneaky-important one — `verify(repository, never()).save(any())`: on failure, *nothing gets persisted*. `never()` turns verify into a prohibition. This test used matchers (`anyString()`, `anyDouble()`, `any()`) instead of literals; both are fine — but never mixed in one call, a rule exercise 3 will make memorable.

---

## Part 5 — The must-not-charge case

The reverse prohibition: a non-OPEN order must be refused *before* any money moves.

```java
@Test
void nonOpenOrderIsRefusedAndNeverCharged() {
    Order shipped = new Order("A-2", "cust-42", 60.00, "SHIPPED");
    when(repository.findById("A-2")).thenReturn(Optional.of(shipped));

    assertThrows(IllegalStateException.class, () -> service.checkout("A-2"));

    verify(gateway, never()).charge(anyString(), anyDouble());
}
```

*The test that keeps customers from being double-charged: the exception is half the contract, the `never()` is the other half.*

Notice we stubbed only `findById` — nothing else gets called, so nothing else needs a script. Stub what the test needs, no more; a wall of stubs is a design review.

---

## Part 6 — Rich assertions on what was persisted: ArgumentCaptor

`argThat` answers yes/no. When the *saved object itself* deserves a full inspection, capture it:

```java
@Test
void checkoutPersistsThePaidOrderIntact() {
    Order order = new Order("A-1", "cust-42", 120.00, "OPEN");
    when(repository.findById("A-1")).thenReturn(Optional.of(order));
    when(gateway.charge("cust-42", 120.00)).thenReturn(new PaymentResult("TX-9"));

    service.checkout("A-1");

    ArgumentCaptor<Order> savedOrder = ArgumentCaptor.forClass(Order.class);
    verify(repository).save(savedOrder.capture());

    Order saved = savedOrder.getValue();
    assertAll("persisted order",
        () -> assertEquals("PAID", saved.getStatus()),
        () -> assertEquals("TX-9", saved.getTransactionId()),
        () -> assertEquals(120.00, saved.getTotal(), 0.001),
        () -> assertEquals("cust-42", saved.getCustomerId()));
}
```

*`capture()` in the verify, `getValue()` after — then assert with the full JUnit vocabulary, including that nothing was mangled on the way through.*

(Import `org.mockito.ArgumentCaptor`.) The last two assertions are the underrated ones: total and customer *unchanged*. Tests that only check what should change miss the bug that changes something else. Run the full suite: everything green — six tests, none of which opened a socket, touched a disk, or charged a card.

---

## Exercises

The training wheels come off. The first is the big one.

1. **The cancellation flow.** `OrderService.cancel(orderId)` exists and is deliberately untested. Its intended behavior: *cancelling an OPEN order marks it CANCELLED and saves it — no money moves; cancelling a PAID order refunds the original transaction through the gateway, marks the order REFUNDED, and saves it; cancelling an order in any other status throws `IllegalStateException` and changes nothing; a nonexistent order throws `OrderNotFoundException`.* Write the test class for that spec. The design work is yours: for each scenario decide what to stub, what to `verify`, where `never()` or `verifyNoInteractions` earns its place, and what to leave unverified on purpose. Aim for one behavior per test, named accordingly.

2. **Deflate the over-mocked test.** `ReviewMockitoTest.checkoutDoesEverythingExactlyLikeTheCurrentImplementation` passes — and is still a bad test. List what it verifies that it shouldn't, what it fails to assert that it should, and rewrite it as one or two outcome-focused tests. Then answer in a comment: which *specific* refactors of `checkout` (that keep behavior identical) would break the original test but not yours?

3. **The matcher rule.** Enable the `@Disabled` test in `ReviewMockitoTest`, run it, and read the exception it produces — name and all. Fix the single broken line, and write the rule in a comment in your own words, including *why* Mockito can't support the half-and-half form.

4. **The unknown order.** One checkout scenario went untested in the walkthrough: the order id that doesn't exist. Write it — remember what an unstubbed mock returns for an `Optional`-returning method, and decide what the strongest possible claim about the gateway is for this scenario (there's something stronger than `never().charge(...)`).
