# Annotations at Runtime

The Annotations lesson (Unit 2) ended on a promise: labels are read by tools, and the runtime readers use reflection. Both halves are now in hand. This lesson closes the loop — how annotations are declared to *survive* into the running JVM, how reflective code discovers them, and the label-plus-reader pattern assembled end to end. After this, no framework in the rest of the unit is magic.

---

## Retention: Which Annotations Survive

An annotation type declares its lifespan with **`@Retention`**:

- **`SOURCE`** — compiler input only, gone from the `.class` file. (`@Override` — its whole job is a compile-time check.)
- **`CLASS`** — in the bytecode, invisible at runtime. The default; mostly for bytecode tools.
- **`RUNTIME`** — loaded with the class, queryable by reflection. **Every framework annotation lives here** — `@Test`, `@Entity`, `@Autowired`, `@RestController`.

Its companion **`@Target`** restricts *where* the annotation may be written (`TYPE`, `METHOD`, `FIELD`, `PARAMETER`...). A framework-grade annotation declaration reads:

```java
@Retention(RetentionPolicy.RUNTIME)          // survive into the JVM
@Target(ElementType.METHOD)                  // methods only
public @interface Scheduled {
    long everySeconds();
    boolean onlyWeekdays() default false;    // elements can have defaults
}
```

*A runtime-visible, method-only annotation with two configuration elements — the standard template.*

```java
public class Housekeeping {
    @Scheduled(everySeconds = 3600)
    public void purgeExpiredSessions() { ... }

    @Scheduled(everySeconds = 86_400, onlyWeekdays = true)
    public void emailDailyReport() { ... }
}
```

*The label applied: metadata attached to methods, awaiting a reader.*

---

## Reading Annotations Reflectively

Every reflective element — `Class`, `Method`, `Field`, `Constructor`, `Parameter` — implements `AnnotatedElement`, a small query API:

```java
Method m = Housekeeping.class.getMethod("purgeExpiredSessions");

if (m.isAnnotationPresent(Scheduled.class)) {          // is the label there?
    Scheduled sched = m.getAnnotation(Scheduled.class); // get it — typed!
    long interval = sched.everySeconds();               // elements read like getters
}
```

*The three-step read: check presence, fetch the annotation object, read its elements.*

Note the pleasant surprise: `getAnnotation` returns a **typed** object — `Scheduled`, with its elements as methods. Reflection's usual `Object`-juggling doesn't apply here; annotation reading is type-safe. `getAnnotations()` lists everything on an element, and `getAnnotationsByType` handles repeatable annotations.

---

## The Full Pattern: A Mini-Framework

Label plus reader, assembled — a scheduler that discovers its work the way JUnit discovers tests:

```java
static void registerAll(Object component, ScheduledExecutorService pool) {
    for (Method m : component.getClass().getDeclaredMethods()) {
        Scheduled sched = m.getAnnotation(Scheduled.class);
        if (sched == null) continue;                   // unlabeled — not ours

        m.setAccessible(true);
        pool.scheduleAtFixedRate(() -> {
            try {
                m.invoke(component);                   // run the labeled method
            } catch (Exception e) {
                log.error("scheduled task failed: " + m.getName(), e.getCause());
            }
        }, 0, sched.everySeconds(), TimeUnit.SECONDS);
    }
}

registerAll(new Housekeeping(), pool);                 // both @Scheduled methods now run themselves
```

*Twenty lines of framework: scan methods, filter by annotation, configure behavior from its elements, invoke reflectively.*

Every ingredient is from this unit — member enumeration and `invoke` (last lesson), the executor pool (Concurrency), exception unwrapping via `getCause()`. And this *is* the shape of the real things:

- **JUnit**: scan for `@Test`, invoke each on a fresh instance, record outcomes (next topic).
- **Spring**: scan the classpath for `@Component`, construct instances, inject fields marked `@Autowired` (Spring Core).
- **JPA/Hibernate**: read `@Entity`/`@Column` to map classes to tables (Spring Data).
- **Jackson**: serialize fields per `@JsonProperty`, `@JsonIgnore` (Spring Web, under the hood).

---

## Design Notes for the Road

Annotations configure; they don't compute — an annotation is inert data, and *all* behavior lives in the reader, a division to preserve when designing our own (annotate the *what*, keep the *how* in code). Scanning is startup-time work: real frameworks index and cache what reflection finds, so the runtime cost lands once, not per request. And declarative style inverts control — the framework calls *our* labeled code, not vice versa — which is the exact idea, named and generalized, that opens Spring Core: inversion of control. The magic show is over; from here on, the frameworks are just well-engineered applications of this topic.
