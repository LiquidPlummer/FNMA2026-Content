# Assertions & Test Structure

A test is only as good as its assertion — the executable claim at its heart — and only as maintainable as its structure. This lesson fills out the assertion vocabulary JUnit provides, then the discipline of arranging tests so that a failure two years from now diagnoses itself.

---

## The Assertion Vocabulary

All static methods on `org.junit.jupiter.api.Assertions`, all following the same convention — **expected value first, actual second**, optional message last:

```java
assertEquals(150.0, acct.getBalance());              // equality via equals()
assertEquals(0.333, ratio, 0.001);                   // doubles: ALWAYS with a tolerance (Unit 2's warning)
assertNotEquals(oldId, newId);

assertTrue(acct.isActive());                         // boolean claims
assertFalse(names.isEmpty());

assertNull(repo.findGhost());                        // presence
assertNotNull(receipt.getId());

assertSame(expected, actual);                        // == : the SAME object (identity, not equals)
assertArrayEquals(new int[]{1, 2}, result);          // arrays: element-wise
assertIterableEquals(List.of("a", "b"), actual);     // collections: order + contents
```

*The core claims: equality, truth, presence, identity — mapped one-to-one to the comparison semantics from earlier units.*

Getting expected/actual backwards doesn't change pass/fail, but it garbles every failure message ("expected 152 but was 150" vs. the reverse) — worth the pedantry. The floating-point overload with a delta is mandatory for `double`s; `assertEquals(0.3, 0.1 + 0.2)` fails for exactly the IEEE-754 reasons from the Primitives lesson.

Two assertions deserve their own paragraph. **`assertThrows`** claims a failure contract and *returns the exception* for further claims:

```java
IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> acct.deposit(-5.0));

assertTrue(ex.getMessage().contains("positive"));    // the message is part of the contract too
```

*Testing the throw AND the diagnostic quality — the fail-fast design from Unit 2, verified.*

And **`assertAll`** groups related claims so *all* are evaluated even when one fails — the fix for the annoyance where a test dies on assertion one of five:

```java
assertAll("receipt",
    () -> assertEquals("ACCT-7", receipt.accountId()),
    () -> assertEquals(150.0, receipt.amount()),
    () -> assertNotNull(receipt.timestamp()));
```

*Grouped assertions: one run reports every discrepancy, not just the first.*

---

## Arrange–Act–Assert

The universal shape of a readable test — three phases, in order, visually separated:

```java
@Test
void withdrawalBelowBalanceSucceeds() {
    // Arrange — build the world
    BankAccount acct = new BankAccount("Ada", 100.0);

    // Act — the ONE behavior under test
    acct.withdraw(30.0);

    // Assert — verify the outcome
    assertEquals(70.0, acct.getBalance());
}
```

*Arrange–Act–Assert (a.k.a. Given–When–Then): setup, a single action, verification — no other traffic.*

The discipline sounds trivial and pays compound interest. **One behavior per test**: when `withdrawalBelowBalanceSucceeds` fails, the culprit is withdrawals — a test asserting deposits *and* withdrawals *and* fees fails ambiguously. **The Act phase is one line** ideally; more means the test is testing a workflow, not a unit. **Assert the outcome, not the journey** — check the resulting state, not incidental internals that refactoring will legitimately change (over-specified tests fail when *nothing is wrong*, and teams learn to ignore them — the worst outcome a suite can have).

Test the boundaries, not just the sunny path. For `withdraw`: exact balance (succeeds? fee?), zero, negative, more-than-balance, on a frozen account. The edges from every unit so far — nulls, empties, off-by-ones, overflow — are precisely where bugs live and where tests earn their keep. A useful heuristic: every `if` in the method under test wants at least one test per branch.

---

## Structure at Suite Scale

Conventions that keep a thousand-test suite navigable:

- **One test class per production class** (`BankAccountTest` for `BankAccount`), same package in the test root — granting package-private visibility when needed.
- **`@Nested` classes group scenarios** — `class Withdrawals { ... }`, `class FrozenAccounts { ... }` — turning reports into an outline of behavior.
- **Helper methods for deep arranging** — a private `accountWithHistory(...)` builder beats fifteen setup lines repeated; if many classes need it, a shared *test fixture* class.
- **No logic in tests.** A test with `if`/`for`/`try` is computing its expectation — and can be wrong the same way the code is. Expected values are *literals*, worked out by hand. (Repetition across inputs is what `@ParameterizedTest` is for.)

The remaining obstacle to "fast and isolated": the class under test that *depends* on slow, real things — repositories, HTTP clients, clocks. Purpose-built fakes for every dependency get old fast; the industrial solution is generating them on the fly — Mockito, next.
