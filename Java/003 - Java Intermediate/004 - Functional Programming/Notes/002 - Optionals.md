# Optionals

Absent values have haunted every unit so far: `Map.get` returning `null`, unboxing NPEs, sentinel `-1`s that callers forget to check. The root problem is that a return type of `User` says nothing about whether "no user" is possible — the possibility lives in Javadoc, memory, or a production stack trace. **`Optional<T>`** moves it into the type system: a small container that either holds one non-null value or is **empty**, forcing every caller to acknowledge the empty case before touching the value.

---

## Creating and Returning Optionals

`Optional` is above all a **return type** — the honest signature for any lookup that can miss:

```java
public Optional<User> findByEmail(String email) {
    User u = database.query(email);                 // may be null
    return Optional.ofNullable(u);                  // wraps value, or empty if null
}
```

*The finder pattern: `ofNullable` converts the nullable world into the Optional world at the boundary.*

The three factories: **`Optional.of(value)`** for a value known non-null (throws NPE immediately if not — fail-fast), **`Optional.ofNullable(value)`** when null is possible, **`Optional.empty()`** for explicit absence. A signature returning `Optional<User>` *cannot be misread* — the compiler makes the caller unwrap it, and the "what if there's none?" conversation happens at compile time.

---

## Consuming: The Right Ways Out

The unwrapping API is where `Optional` pays off — or gets abused. In rough order of preference:

```java
Optional<User> found = repo.findByEmail("ada@example.com");

// Substitute a default
User user = found.orElse(User.GUEST);
User user2 = found.orElseGet(() -> expensiveDefault());     // Supplier: built only if needed

// Escalate absence into a proper failure
User user3 = found.orElseThrow(() -> new UserNotFoundException("ada@example.com"));

// Act only when present
found.ifPresent(u -> sendWelcome(u));
found.ifPresentOrElse(u -> sendWelcome(u), () -> log.warn("no such user"));
```

*Four idioms: default, lazy default, throw, and conditional action — each names its empty-case policy explicitly.*

Note the functional interfaces from last lesson doing the work: `orElseGet` takes a `Supplier` (deferring cost until actually needed — prefer it over `orElse` when the default is expensive), `ifPresent` a `Consumer`, `orElseThrow` a `Supplier` of the exception.

The methods to treat with suspicion are the tempting ones. **`get()`** throws `NoSuchElementException` on empty — using it unguarded recreates the NPE problem with a different exception name. And the guard-then-get shape is just null-checking in costume:

```java
if (found.isPresent()) {               // legal, but misses the point —
    send(found.get());                 // ...this is a null check wearing a hat
}
found.ifPresent(u -> send(u));         // same logic, no escape hatch left open
```

*`isPresent()`/`get()` works but reintroduces the forgettable check; the callback form can't be forgotten.*

---

## Transforming Without Unwrapping

The power move: operate on the *maybe-value* directly, deferring the unwrap. **`map`** transforms the contents if present (empty stays empty); **`filter`** empties the Optional if a test fails; **`flatMap`** chains methods that themselves return Optionals:

```java
String domain = repo.findByEmail("ada@example.com")
        .map(User::getEmail)                       // Optional<String> — or empty
        .map(e -> e.substring(e.indexOf('@') + 1)) // still riding the maybe
        .filter(d -> !d.isBlank())
        .orElse("unknown");
```

*A pipeline over a possibly-absent value: transformations apply only if something's there; one default at the end.*

Compare the null-checked equivalent — three nested `if (x != null)` levels — and the appeal is obvious: **absence is handled once, at the end, instead of at every step.** (`User::getEmail` is a method reference — lesson 3 formalizes it.) `flatMap` earns its place when a chain would otherwise produce `Optional<Optional<T>>`: `findUser(id).flatMap(User::getManager)` where `getManager` returns `Optional<User>`.

---

## Where Optionals Belong — and Don't

`Optional` has a deliberately narrow job description, and the conventions are firm:

- **Yes: return types** of anything that can legitimately find nothing. This is the designed use, full stop.
- **No: fields.** An `Optional` field triples the states (null Optional, empty, present) and isn't serializable-friendly; use a nullable field, exposing `Optional` from the *getter* if desired.
- **No: method parameters.** `process(Optional<Config> cfg)` forces every caller to wrap; overload the method or accept null with documentation instead.
- **No: collections.** `List<Optional<User>>` should almost always just be a shorter `List<User>`.
- **Not a null-eradicator.** References elsewhere remain nullable; `Optional` marks the *designed* absence points, particularly API boundaries.

Primitive flavors exist (`OptionalInt`, `OptionalDouble` — met in Streams' `average()` back in the Arrays topic, and returning shortly). The mental model to carry forward: `Optional` is a **stream of zero or one element** — same `map`/`filter` vocabulary, same pipeline thinking — which makes the jump to full Streams in lesson 4 half-done already.
