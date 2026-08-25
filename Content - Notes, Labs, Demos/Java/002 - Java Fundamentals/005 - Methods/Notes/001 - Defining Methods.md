# Defining Methods

A **method** is a named, reusable block of behavior attached to a class — Java's version of a function. We've defined `main` and a few helpers already; this topic slows down and treats methods properly, starting with the anatomy of a definition.

---

## The Anatomy

```java
public static int applyDiscount(int price, int percent) {
    int discount = price * percent / 100;
    return price - discount;
}
```

*Modifiers, return type, name, parameter list, body — every method definition is these five parts.*

Reading left to right:

- **Modifiers** — `public` (who may call it — the access modifiers from Hello World, formalized in Classes & Objects) and optionally `static` (belongs to the class rather than an instance; also formalized there). Order convention: access modifier first.
- **Return type** — the type of value the method hands back, or **`void`** for "nothing."
- **Name** — a verb or verb phrase in `camelCase`: `calculateTax`, `isEmpty`, `sendReceipt`. Methods *do* things; their names should say what.
- **Parameter list** — zero or more typed inputs, in parentheses. Empty parentheses are still required: `getTime()`.
- **Body** — the statements in braces, plus `return` where a value is produced.

The name and parameter types together form the method's **signature** — `applyDiscount(int, int)` — which is how the compiler identifies which method a call refers to. Return type and parameter *names* are not part of it.

---

## Where Methods Live

Methods are always defined directly inside a class — never nested in another method, never floating outside a class:

```java
public class Billing {

    public static int applyDiscount(int price, int percent) {
        return price - (price * percent / 100);
    }

    public static boolean isFreeOrder(int price) {
        return applyDiscount(price, 100) == 0;    // methods in the same class call each other by name
    }
}
```

*Two methods in one class; definition order doesn't matter — `isFreeOrder` could come first.*

For now, our methods are `static` like `main`, which lets them call each other directly. **Instance methods** — the non-static kind that operate on an object's state — are the heart of the Classes & Objects topic, and everything in this topic (parameters, returns, signatures) applies to them unchanged.

---

## Why Extract a Method

Mechanically, a method definition does nothing until called — defining is *declaring capability*. The reasons to pull code into a method are the working vocabulary of code review:

- **Reuse** — logic written once, called from many places; a fix lands everywhere at once.
- **Naming** — `isEligibleForRefund(order)` documents itself; the same six conditions inline don't.
- **Testing** — a method is a unit that can be exercised in isolation (the Unit Testing topic depends on this).
- **Scope control** — locals inside the method can't leak or collide (the call-stack mechanics from Control Flow).

The craft guideline: a method should do **one thing at one level of detail**, and its name should tell the truth about what that is. Twenty lines is a soft ceiling worth feeling bad about crossing; three well-named ten-line methods beat one thirty-line block nearly every time.

---

## Overloading: One Name, Several Signatures

Java allows multiple methods with the same name in one class, as long as their parameter lists differ — **overloading**. The compiler picks the version whose signature matches the call:

```java
static double area(double radius) { return Math.PI * radius * radius; }
static double area(double width, double height) { return width * height; }

area(2.0);        // circle version
area(3.0, 4.0);   // rectangle version
```

*Same name, different parameter lists — the argument count and types select the method.*

Overloads should be *variations of the same idea* (same behavior, different inputs — `println` has ten of them). Two unrelated behaviors sharing a name is technically legal and genuinely hostile. Note that differing only in return type doesn't compile — the return type isn't in the signature, so the compiler couldn't tell the calls apart.

---

## Defining vs. Calling

A definition is inert text until something invokes it — and a program is ultimately one entry-point call (`main`) fanning out through definitions via calls. The call side has its own mechanics and conventions, and that's the next lesson.
