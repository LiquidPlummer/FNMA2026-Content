# Lab: Build a Mini-Framework — Reflection in Three Acts

The Reflection notes end on a promise: *no framework is magic after this*. This lab collects on it, in code. In Act 1 we interrogate a class at runtime — `Class` objects, fields, methods, modifiers. In Act 2 we write the ten-line universal serializer that is secretly the skeleton of every JSON library. In Act 3 we define our own `@Audited` annotation and write the twenty-line runner that discovers and invokes annotated methods — a working annotation framework, hand-built, the same shape as JUnit and Spring.

---

## Prerequisites

| Software | Required Version |
|---|---|
| JDK (Temurin or any OpenJDK build) | 21 (LTS) |
| Apache Maven | 3.9.x |

Verify both with `java -version` and `mvn -version`. Then, from this lab's folder (the one containing `pom.xml`), run:

```console
mvn -q compile exec:java -Dexec.args="1"
```

You should see the Act 1 banner and nothing else — the acts are empty until you build them. Three files to know about:

- [src/main/java/com/curriculum/labs/ReflectionLab.java](src/main/java/com/curriculum/labs/ReflectionLab.java) — where you work; find the `[MARKER]` comments.
- [src/main/java/com/curriculum/labs/BankAccount.java](src/main/java/com/curriculum/labs/BankAccount.java) — the specimen for Acts 1 and 2. **Provided; don't edit.** It has private fields, a private helper, a static field, and an interface, all there to be discovered.
- [src/main/java/com/curriculum/labs/PayrollService.java](src/main/java/com/curriculum/labs/PayrollService.java) — the target for Act 3. Provided *without* annotations; adding them is part of the act.

---

## Act 1 — Interrogating a class

### Three roads to the Class object

For every type the JVM has loaded, it maintains exactly **one** `Class` object describing it. At **`[MARKER 1a]`** in `act1()`, we'll reach that object all three ways and prove they arrive at the same place:

```java
Class<BankAccount> fromLiteral = BankAccount.class;

Object something = new BankAccount("Ada", 100.0);
Class<?> fromObject = something.getClass();

Class<?> fromName = Class.forName("com.curriculum.labs.BankAccount");

System.out.println("literal:  " + fromLiteral);
System.out.println("getClass: " + fromObject);
System.out.println("forName:  " + fromName);
System.out.println("same object? " + (fromLiteral == fromObject && fromObject == fromName));
```

*The literal for compile-time-known types, `getClass()` for live objects, `forName` for names that arrive as strings.*

Run Act 1. `same object? true` — and that's `==`, reference identity, not `equals`. One type, one `Class` object, however you reach it. Note also what the variable declarations already teach: the literal keeps its type parameter (`Class<BankAccount>`), while `getClass()` and `forName` return `Class<?>` — "a Class of some unknown type" — because their answers genuinely aren't known until runtime. And try misspelling the string in `forName` once: the failure is a runtime `ClassNotFoundException`, not a compile error. That trade — flexibility for compile-time safety — is reflection's entire price tag, met in the first ten lines.

### The class report

Now the interrogation. At **`[MARKER 1c]`** (below `act1`, as its own method), we'll build a report generator that works on *any* class:

```java
static void classReport(Class<?> c) {
    System.out.println("class:      " + c.getName());
    System.out.println("simple:     " + c.getSimpleName());
    System.out.println("superclass: " + c.getSuperclass().getSimpleName());
    System.out.print("interfaces: ");
    for (Class<?> face : c.getInterfaces()) {
        System.out.print(face.getSimpleName() + " ");
    }
    System.out.println();

    System.out.println("declared fields:");
    for (Field f : c.getDeclaredFields()) {
        System.out.printf("  %-16s %-12s %s%n",
                Modifier.toString(f.getModifiers()), f.getType().getSimpleName(), f.getName());
    }

    System.out.println("declared methods:");
    for (Method m : c.getDeclaredMethods()) {
        System.out.printf("  %-16s %-12s %s%s%n",
                Modifier.toString(m.getModifiers()), m.getReturnType().getSimpleName(),
                m.getName(), Arrays.toString(m.getParameterTypes()));
    }

    System.out.println("getMethods() count          (public, inherited included): " + c.getMethods().length);
    System.out.println("getDeclaredMethods() count  (all, this class only):       " + c.getDeclaredMethods().length);
}
```

*Names, hierarchy, members, modifiers — the compile-time facts, recited at runtime.*

You'll need imports: `java.lang.reflect.Field`, `java.lang.reflect.Method`, `java.lang.reflect.Modifier`, and `java.util.Arrays`.

Then at **`[MARKER 1b]`**, call it: `classReport(BankAccount.class);`

Run Act 1 and read the report against the `BankAccount` source, side by side. Everything private is *in* the report — the `balance` and `frozen` fields, the `requirePositive` helper — because `getDeclaredX` means "everything declared here, access be damned." Meanwhile the two counts at the bottom tell the other half of the story: `getMethods()` is the larger number because it includes public methods *inherited* from `Object` (`equals`, `hashCode`, `wait`, and friends), while `getDeclaredMethods()` is this class only. That `getX`-vs-`getDeclaredX` distinction trips up everyone once; you now get to be trip-proofed cheaply.

This report is exactly how your code looks to a framework that has never heard of it: a name, a parent, a bag of members to enumerate. Hold that thought through the next two acts.

---

## Act 2 — The universal serializer

Ten lines that process objects they have never seen. At **`[MARKER 2a]`**:

```java
static Map<String, Object> toMap(Object obj) throws IllegalAccessException {
    Map<String, Object> out = new LinkedHashMap<>();
    for (Field field : obj.getClass().getDeclaredFields()) {
        if (Modifier.isStatic(field.getModifiers())) continue;   // instance state only
        field.setAccessible(true);                                // disarm the private check
        out.put(field.getName(), field.get(obj));
    }
    return out;
}
```

*Enumerate the fields, skip statics, force access, read values — any object in, name→value map out.*

(Add `java.util.Map` and `java.util.LinkedHashMap` to the imports.) Two lines carry the weight. `setAccessible(true)` is the encapsulation bypass the notes warned about — without it, `field.get(obj)` on private `balance` throws `IllegalAccessException`. And the static check matters: `accountsOpened` belongs to the class, not to any account, so a serializer that included it would be lying about the object.

At **`[MARKER 2b]`** in `act2()`, feed it:

```java
BankAccount acct = new BankAccount("Ada", 100.0);
acct.deposit(50);
Map<String, Object> map = toMap(acct);
System.out.println(map);

for (var entry : map.entrySet()) {
    System.out.println("  " + entry.getKey() + " is a "
            + entry.getValue().getClass().getSimpleName());
}
```

*Serialize an account, then ask each map value what it actually is.*

Run Act 2: `{owner=Ada, balance=150.0, frozen=false}` — private state, extracted by code that has no idea what a `BankAccount` is. Now look at the type lines: `balance is a Double`, `frozen is a Boolean`. The field is a primitive `double`, but `Field.get` returns `Object`, and primitives can't be `Object`s — so reflection **boxed** them in transit. The wrapper classes, load-bearing again, invisibly.

To feel the universality, define a small record anywhere in `ReflectionLab` — `record Order(String id, int qty) { }` — and add `System.out.println(toMap(new Order("A-17", 3)));`. Same ten lines, never modified, new type handled: `{id=A-17, qty=3}`.

One boundary worth hitting on purpose. Add — temporarily — `System.out.println(toMap(java.time.LocalDate.now()));` and run. This dies with `InaccessibleObjectException`, and the message is worth reading in full: *module java.base does not "opens java.time" to unnamed module*. `LocalDate`'s fields are private members of a *JDK module*, and the module system refuses to let `setAccessible` force them open. Our own classes said yes; `java.base` says no. That's the "within limits the module system now enforces" caveat from the notes, experienced firsthand. (Fun fact: `toMap("hello")` would *work* here and spill `String`'s internal `byte[]` — not because `String` is fair game, but because Maven's own launcher passes `--add-opens java.base/java.lang=ALL-UNNAMED` for its internal machinery, and `exec:java` inherits it. Frameworks poke exactly these holes, exactly this way.) Delete the line once you've seen the message — but remember its shape; it shows up in real projects whenever a serializer meets a JDK type it shouldn't be reflecting into.

---

## Act 3 — The annotation framework

The real frameworks don't process *every* member — they process the ones wearing a label: `@Test`, `@Entity`, `@Autowired`. Time to build the label and the reader, end to end.

### The label

Create a **new file**, `src/main/java/com/curriculum/labs/Audited.java`:

```java
package com.curriculum.labs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)          // survive into the running JVM
@Target(ElementType.METHOD)                  // methods only
public @interface Audited {
    String reason();                         // why this method is audit-worthy
}
```

*A runtime-visible, method-only annotation with one required element — the standard framework template.*

The line that makes everything work is `@Retention(RetentionPolicy.RUNTIME)`. Without it, the annotation defaults to `CLASS` retention: present in the bytecode, *invisible to reflection*. Keep that in your pocket for exercise 4.

Now apply the label. In `PayrollService.java`, annotate three of the four methods — leave `previewPayroll` bare, a preview isn't audit-worthy:

```java
@Audited(reason = "money moves")
public int runPayroll() { ... }

@Audited(reason = "regulatory filing")
public String exportTaxReport() { ... }

@Audited(reason = "books locked")
private String closeQuarter() { ... }
```

*Metadata attached to methods, awaiting a reader. Note one of them is private.*

### The reader

At **`[MARKER 3a]`**, the twenty lines that make it all real:

```java
static void runAudited(Object target) throws Exception {
    for (Method m : target.getClass().getDeclaredMethods()) {
        Audited audited = m.getAnnotation(Audited.class);
        if (audited == null) continue;                       // unlabeled — not ours

        m.setAccessible(true);
        System.out.println("AUDIT  invoking " + m.getName()
                + "  (reason: " + audited.reason() + ")");
        try {
            Object result = m.invoke(target);
            System.out.println("AUDIT  " + m.getName() + " returned: " + result);
        } catch (InvocationTargetException e) {
            System.out.println("AUDIT  " + m.getName() + " FAILED: " + e.getCause());
        }
    }
}
```

*Scan the methods, filter by annotation, configure behavior from the annotation's element, invoke reflectively — the label-plus-reader pattern, complete.*

(One more import: `java.lang.reflect.InvocationTargetException`.) Three details to appreciate as you type. `getAnnotation` returns a **typed** object — `audited.reason()` reads like a getter, no casting, the one corner of reflection that isn't `Object`-juggling. The `null` check *is* the filter — this is precisely how JUnit decides what's a test. And the catch clause unwraps `InvocationTargetException.getCause()`: when a reflectively-invoked method throws, the real exception arrives gift-wrapped, and frameworks that forget to unwrap produce the useless stack traces we've all squinted at.

At **`[MARKER 3b]`**: `runAudited(new PayrollService());`

Run Act 3:

- `runPayroll`, `exportTaxReport`, and `closeQuarter` each get invoked with their reason and result logged — including the **private** one, courtesy of `setAccessible`.
- `previewPayroll` is silently skipped. No annotation, no invocation. The framework ignored what wasn't labeled — which is the entire contract.
- Run it twice more: the order of the three audited calls may differ between runs — `getDeclaredMethods` promises **no particular order**. Real frameworks sort or index what they scan; ordering by declaration position is not something reflection gives you for free.

Step back and look at what you built: a working annotation framework in about thirty lines, using nothing but this topic. JUnit scans for `@Test` and invokes; Spring scans for `@Component` and constructs; JPA reads `@Entity` and maps. Same loop, industrialized. The magic show is over.

---

## Exercises

The training wheels come off — same machinery, new problems, no step-by-step. Work in new files or new parts as you see fit.

1. **The `@Default` injector.** Define a field-targeted, runtime-retained annotation `@Default` with a single `String value()`. Then write `static void injectDefaults(Object target)` that walks the target's declared fields and, for each field carrying `@Default`, parses the string to the field's type and *writes* it with `field.set(...)`. Support at least `String`, `int`, `double`, and `boolean` (hint: `field.getType() == int.class`, then `Integer.parseInt`). Prove it on a test-data class of your own devising — `new TestOrder()` with blank fields going in, a fully-populated object coming out. You've just written the heart of a test-fixture library.

2. **Deep serializer.** `toMap` has a blind spot: it reads `getDeclaredFields()` of the runtime class only, so inherited state is missing. Create `class SavingsAccount extends BankAccount` with one extra field (say `double rate`), then fix `toMap` to walk the superclass chain (`getSuperclass()` in a loop, stopping at `Object`) so a `SavingsAccount` serializes with **all** its state. Decide and defend: if a subclass shadows a parent's field name, which value wins in your map — and does `LinkedHashMap.put` agree with your intent?

3. **The kill switch.** Add a second element to `@Audited`: `boolean enabled() default true`. Update the runner to *skip* methods annotated with `enabled = false` — but not silently: log `AUDIT  skipping <name> (disabled)`. Verify existing annotations still work without touching them (that's what the default is for), then disable one method and watch it sit out. One question to answer in a comment: why must the *reader* implement the skip — why can't the annotation do it itself?

4. **Why did the framework skip my method?** A debugging scenario, answered in a comment — no fair running it first. *A teammate defines the annotation below, labels a method with it, and runs your Act 3-style scanner. The scanner finds nothing. The code compiles clean; the annotation is definitely on the method — they can see it right there in the source.*

   ```java
   @Target(ElementType.METHOD)
   public @interface Timed {
       long warnAboveMillis() default 500;
   }
   ```

   Explain precisely why the scanner sees nothing, name the default that's biting them, give the one-line fix — and then answer the follow-up: why does `@Override` get away with never declaring `RUNTIME` retention?
