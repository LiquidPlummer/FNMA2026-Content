# Lab: Red, Green, Prove It — JUnit Basics, Assertions & Test Structure

In this lab we put a provided-but-untested `PromoCode` class under test: the first `@Test` with Arrange–Act–Assert structure, the assertion vocabulary (`assertEquals` with a delta, `assertThrows` with a message check, `assertAll`), `@BeforeEach` extraction, and a `@ParameterizedTest` over boundary values. Fair warning that is also the sales pitch: **the class has two planted bugs.** Your tests will catch one during the walkthrough; the other survives until you write the test that corners it.

---

## Prerequisites

| Software | Required Version |
|---|---|
| JDK (Temurin or any OpenJDK build) | 21 (LTS) |
| Apache Maven | 3.9.x |
| JUnit (via `pom.xml`, no install needed) | 5.11.x |

Verify the first two with `java -version` and `mvn -version`. JUnit arrives as a test-scoped Maven dependency — look at [pom.xml](pom.xml) and find `junit-jupiter-api` and `junit-jupiter-params` with `<scope>test</scope>`. Then, from this lab's folder, run the suite:

```console
mvn test
```

You should see `Tests run: 5, Failures: 0, Errors: 0, Skipped: 1` — one starter test in `PromoCodeTest`, four in `ReviewExercisesTest` of which one is deliberately skipped. This is the command you'll run after **every** part below — the whole rhythm of this lab is edit, `mvn test`, read the bar.

The cast:

- [src/main/java/com/curriculum/labs/PromoCode.java](src/main/java/com/curriculum/labs/PromoCode.java) — the class under test. **Contains two bugs.** Resist reading it for bugs now; the point is that tests find them.
- [src/test/java/com/curriculum/labs/PromoCodeTest.java](src/test/java/com/curriculum/labs/PromoCodeTest.java) — where you'll work. Note the layout: same package as `PromoCode`, different source root (`src/test/java`). Tests live *with* the code but not *in* the shipped code.
- [src/main/java/com/curriculum/labs/DateRange.java](src/main/java/com/curriculum/labs/DateRange.java) and [src/test/java/com/curriculum/labs/ReviewExercisesTest.java](src/test/java/com/curriculum/labs/ReviewExercisesTest.java) — exercise material; leave them alone until the end.

### The spec

Tests verify code against *intent*, so here is the intent, as the ticket described it:

> A promo code has a **code** (required, non-blank; stored trimmed and uppercased), a **percent discount** greater than 0 and **up to and including 50**, and a **minimum order amount** (zero or more). `apply(orderTotal)` returns the discounted total for a positive order total **of at least the minimum** — an order exactly at the minimum qualifies. Invalid construction arguments and non-qualifying orders throw `IllegalArgumentException` with a message that says what's wrong. `preview(orderTotal)` returns the code, the amount off, and the final total, without changing anything.

Somewhere in `PromoCode.java`, twice over, the code disagrees with that paragraph.

---

## Part 1 — The first test: Arrange, Act, Assert

Every readable test has the same three-phase shape: build the world, do the one thing, verify the outcome. In `PromoCodeTest`, below the marker comment, we'll add our first real test:

```java
@Test
void twentyPercentComesOffAQualifyingOrder() {
    // Arrange
    PromoCode promo = new PromoCode("SAVE20", 20, 50);

    // Act
    double discounted = promo.apply(80.0);

    // Assert
    assertEquals(64.0, discounted, 0.001);
}
```

*Arrange–Act–Assert, visually separated: one object, one action, one claim.*

Run `mvn test` — 6 tests now, no failures. Two things to notice while it's fresh. The name states a **behavior**, not a method (`twentyPercentComesOffAQualifyingOrder`, not `testApply`) — when this fails in CI two years from now, the name *is* the bug report. And the assertion has three arguments: expected first, actual second, and a **delta** of `0.001` — `80.0 * 0.80` happens to land exactly on `64.0`, but doubles in general don't (IEEE-754, from the Primitives lesson), so the tolerance form is mandatory habit for floating-point money like this.

The expected value `64.0` was computed by hand — 20% of 80 is 16, 80 minus 16 is 64 — not by re-running the production formula. A test that computes its expectation with the code's own logic can be wrong the same way the code is. Hold that thought for exercise 3.

---

## Part 2 — Failure contracts: assertThrows

The spec doesn't just say what works — it says what *refuses*, and refusal is behavior worth pinning down. Two tests to add:

```java
@Test
void blankCodeIsRejected() {
    assertThrows(IllegalArgumentException.class,
            () -> new PromoCode("   ", 20, 50));
}

@Test
void orderBelowMinimumIsRejectedWithAHelpfulMessage() {
    PromoCode promo = new PromoCode("SAVE20", 20, 50);

    IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> promo.apply(20.0));

    assertTrue(ex.getMessage().contains("minimum"));
}
```

*The failure contract, verified: the exception type AND the diagnostic quality.*

(Extend the static import to `assertTrue`, or switch to `import static org.junit.jupiter.api.Assertions.*;` — the usual style in test classes.) The second test shows `assertThrows`'s underrated feature: it *returns* the caught exception, so we can make claims about the message. A method that fails with a useless message has a bug of a different kind — the fail-fast design from earlier units is only as good as its diagnostics, and here the message is part of the contract.

Run `mvn test`: 8 run, no failures.

---

## Part 3 — Several claims about one outcome: assertAll

`preview` returns a `Discount` record with three components — three claims about one result. Chain three separate `assertEquals` calls and the test dies on the first failure, hiding the other two. `assertAll` evaluates every claim and reports all discrepancies at once:

```java
@Test
void previewBreaksDownTheDiscount() {
    PromoCode promo = new PromoCode("save20", 20, 50);

    PromoCode.Discount discount = promo.preview(80.0);

    assertAll("discount breakdown",
        () -> assertEquals("SAVE20", discount.code()),
        () -> assertEquals(16.0, discount.amountOff(), 0.001),
        () -> assertEquals(64.0, discount.finalTotal(), 0.001));
}
```

*One Act, three grouped Asserts — a failure report that lists every wrong field, not just the first.*

Small detail doing quiet work: we constructed with lowercase `"save20"` and asserted `"SAVE20"` — the normalization rule from the spec, tested for free on the way past. Run it: 9 run, no failures.

---

## Part 4 — Repetition into @BeforeEach

Three of our tests now begin with the same `new PromoCode("SAVE20", 20, 50)`. Repeated arranging moves into a lifecycle method that runs before *every* test:

```java
class PromoCodeTest {

    private PromoCode standardPromo;

    @BeforeEach
    void freshPromo() {
        standardPromo = new PromoCode("SAVE20", 20, 50);
    }

    ...
}
```

*`@BeforeEach`: shared setup without shared state — every test still gets a pristine object.*

(Import `org.junit.jupiter.api.BeforeEach`.) Now delete the Arrange line from `twentyPercentComesOffAQualifyingOrder`, `orderBelowMinimumIsRejectedWithAHelpfulMessage`, and any other test that built that same object, and use `standardPromo` instead. Tests that need a *different* promo (like the blank-code test) keep their own arranging — `@BeforeEach` is for the common case, not a straitjacket.

Run `mvn test` again: still 9, still no failures. A refactor of the tests themselves, proven safe by the tests' own bar — the suite protects itself, too. Worth knowing as you do this: JUnit creates a **fresh instance of the test class for every test method**, so `standardPromo` is never shared between tests; order can never matter.

---

## Part 5 — Boundary values, and the first kill

The spec says percent is valid "up to **and including** 50." Boundaries are where bugs live, so let's test a spread of legal values with one parameterized test — one body, many runs:

```java
@ParameterizedTest
@ValueSource(doubles = {0.5, 20.0, 50.0})
void validPercentagesAreAccepted(double percent) {
    PromoCode promo = new PromoCode("PCT", percent, 0);

    assertEquals(percent, promo.getPercentOff(), 0.001);
}
```

*Table-driven coverage: the smallest sensible value, a middle value, and the boundary itself.*

(Imports: `org.junit.jupiter.params.ParameterizedTest` and `org.junit.jupiter.params.provider.ValueSource`.) Run `mvn test`.

**Red.** Two of the three runs pass; `[3] 50.0` fails with `IllegalArgumentException: percentOff must be between 0 (exclusive) and 50 (inclusive), got 50.0`. Read that failure slowly, because it's a beauty: the exception *message* states the spec correctly — inclusive — while the code that throws it disagrees. Open `PromoCode.java`, find the constructor's percent check, and there it is: `percentOff >= 50` where the spec demands `percentOff > 50`. The classic off-by-one, at the boundary, exactly where the parameterized test aimed.

Fix the production code (change `>=` to `>`), run `mvn test`: **green** — `Tests run: 12, Failures: 0` (each parameterized run counts as a test). That's the loop this lab is named for — red proved the test can fail, green proved the fix, and the test now stands guard forever. Planted bug #1: down.

---

## Part 6 — Renaming test1, an exercise in communication

That leftover `test1()` at the top of the class has been passing this whole time, telling nobody anything. Imagine it red in a CI report: *test1 failed.* Failed at *what*?

Rewrite it — name from the behavior, phases separated:

```java
@Test
void tenPercentComesOffWhenThereIsNoMinimum() {
    PromoCode promo = new PromoCode("SAVE10", 10, 0);

    double discounted = promo.apply(100.0);

    assertEquals(90.0, discounted, 0.001);
}
```

*Same assertion, entirely different information content.*

Run the suite one more time — everything green. Which is precisely the problem: somewhere in `PromoCode`, **bug #2 is still alive**, and a dozen green tests didn't touch it. Here's the only hint the walkthrough will give: we tested an order comfortably *above* the minimum (80 against 50) and one comfortably *below* it (20 against 50). Reread the spec sentence about which orders qualify. What did we never test?

---

## Exercises

The training wheels come off. Exercise 1 finishes the hunt; the rest apply the same discipline to new material.

1. **Corner bug #2.** Write the boundary test the walkthrough hinted at, watch it go red, fix `PromoCode`, watch it go green. The whole point of the exercise is the order: test first, red first, *then* touch the production code. (If your new test passes on the first run, it isn't the test the spec is asking for.)

2. **A full test class for `DateRange`.** The provided [DateRange.java](src/main/java/com/curriculum/labs/DateRange.java) is believed correct — your job is the proof. Write `DateRangeTest` against this behavior spec, with at least the following covered:
   - **Boundaries:** `contains` is inclusive at *both* endpoints — test the start date, the end date, the day before the start, and the day after the end.
   - **Invalid input:** construction with `end` before `start` throws; construction with a `null` date throws; `contains(null)` is simply false (not an exception).
   - **The degenerate case:** a single-day range (`start.equals(end)`) — its length is 1, it contains exactly its one day.
   - **Overlaps:** two ranges sharing only a single endpoint day *do* overlap; disjoint ranges don't; a range fully inside another does; and overlap is symmetric — `a.overlaps(b)` must always equal `b.overlaps(a)`.
   Use everything from the walkthrough where it fits: a parameterized test is a natural fit for the `contains` boundaries, `@BeforeEach` for a standard range, behavior names throughout.

3. **Critique and fix.** The first three tests in [ReviewExercisesTest.java](src/test/java/com/curriculum/labs/ReviewExercisesTest.java) all pass — and each is bad in a distinct way. For each one: identify the smell (the comments above them ask leading questions), explain in a sentence what will make it fail *when nothing is actually wrong* — or fail to fail when something is — and rewrite it into one or more well-shaped tests. The three smells all have names in the notes.

4. **The lying expectation.** Enable the `@Disabled` test at the bottom of `ReviewExercisesTest`. Before running it, compute 12% off $80.00 by hand. Now run it, read the failure message, and decide which side is wrong — the code or the test — and fix the guilty party. A test suite is only trustworthy if its expected values are; "the test failed" and "the code is broken" are not the same sentence.
