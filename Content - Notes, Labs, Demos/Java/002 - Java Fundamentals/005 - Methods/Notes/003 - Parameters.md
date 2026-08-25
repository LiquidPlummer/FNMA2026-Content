# Parameters

**Parameters** are a method's declared inputs; **arguments** are the actual values a caller supplies. The two words get used loosely, but the distinction pays off: parameters live in the definition, arguments live at the call site, and what connects them is a *copy*. That copy — and exactly what gets copied — is the single most misunderstood mechanic in Java.

---

## Java Is Always Pass-by-Value

At every call, each argument is evaluated and its value **copied** into the corresponding parameter. The parameter is a brand-new local variable; reassigning it never affects the caller's variable:

```java
static void bump(int n) {
    n = n + 1;                    // modifies the copy
}

int count = 10;
bump(count);
System.out.println(count);        // 10 — unchanged
```

*The method received a copy of `10`; the caller's `count` was never touched.*

That's uncontroversial for primitives. The confusion starts with objects — and dissolves once we say precisely what the value *is*: for an object argument, the value copied is the **reference** (where the object lives), not the object itself. Method and caller end up with two references to **one shared object**:

```java
static void addSuffix(StringBuilder sb) {
    sb.append("!");               // mutates the SHARED object — caller sees this
}

static void replace(StringBuilder sb) {
    sb = new StringBuilder("new");  // reassigns the local COPY — caller sees nothing
}

StringBuilder text = new StringBuilder("Hi");
addSuffix(text);                  // text now "Hi!"
replace(text);                    // text still "Hi!"
```

*Mutating through the copied reference reaches the shared object; reassigning the copy is invisible outside.*

So the rule has two halves: **a method can change the *state* of objects passed to it, but never which object (or value) the caller's variable holds.** Arrays, being objects, follow the object half — a method handed an array can modify its elements, and callers will see it. Methods that mutate their arguments should say so loudly in their names (`Arrays.sort(data)`); the surprising ones are the quiet ones.

---

## Varargs: Variable-Length Parameters

A method's *last* parameter may be declared with `...`, accepting any number of arguments of that type — **varargs**:

```java
static int sum(int... numbers) {       // numbers is an int[] inside the method
    int total = 0;
    for (int n : numbers) total += n;
    return total;
}

sum();                 // 0 — zero arguments is legal
sum(1, 2);             // 3
sum(1, 2, 3, 4, 5);    // 15
sum(new int[]{1, 2});  // an existing array passes straight through
```

*Inside the method, a varargs parameter is simply an array; callers get the flexible syntax.*

`String.format` and `List.of` are varargs methods we've already met. The constraints: at most one varargs parameter, and it must come last (`static String join(String sep, String... parts)` — fixed parameters first).

---

## How Overload Resolution Picks

With overloading in play (Defining Methods), the compiler matches a call's arguments to the *most specific* applicable signature, at compile time. The rough priority: exact type match first, then widening (`int` argument accepted by a `long` parameter), then autoboxing, then varargs as the last resort. Most of the time this does what anyone would guess; the cases that bite are ambiguous mixes (`call(null)` with several object overloads, or literals near an `int`/`long` boundary), where the fix is an explicit cast to name the intended overload — or better, overloads distinct enough that the question never arises.

---

## Designing Parameter Lists

Working conventions that keep call sites readable:

- **Few parameters.** One to three is comfortable; five-plus is a smell. Java has no named arguments, so `create("A", true, true, false, 3)` tells a reader nothing — bundling related values into a small object (or using a builder, in the Design Patterns topic) restores meaning.
- **Consistent order across overloads and related methods** — target first, options after, is a common house rule.
- **`final` parameters** (`static int fee(final int base)`) forbid reassigning the parameter inside the body. Some teams require it; either way, *treating* parameters as read-only is good hygiene — reassigning a parameter mid-method makes its meaning drift.
- **Validate at the door.** Public methods check their inputs and fail fast with a clear message — which requires `throw`, arriving two lessons from now.
