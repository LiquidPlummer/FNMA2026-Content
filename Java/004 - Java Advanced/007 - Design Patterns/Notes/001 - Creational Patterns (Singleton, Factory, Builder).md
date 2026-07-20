# Creational Patterns (Singleton, Factory, Builder)

**Design patterns** are named, reusable solutions to recurring design problems — a shared vocabulary more than a library ("make the gateway a strategy," "wrap it in a decorator" communicate a design in four words). This topic covers the working set in three families; first the **creational** patterns, which answer one question three ways: *who constructs objects, and how, when plain `new` isn't enough?*

---

## Singleton — Exactly One Instance

**Problem:** some components must exist exactly once — a configuration registry, a connection pool, a cache. **Solution:** the class controls its own instantiation — private constructor, one shared instance:

```java
public final class AppConfig {
    private static final AppConfig INSTANCE = new AppConfig();   // created once, at class load

    private AppConfig() { load(); }                              // nobody else can construct

    public static AppConfig getInstance() { return INSTANCE; }
}

AppConfig.getInstance().get("timeout");
```

*The classic singleton: private constructor, static instance, global access point.*

The static-final-field form shown is the correct hand-rolled version (class loading makes it thread-safe for free — no `synchronized` lazy-init gymnastics needed; an `enum` with one constant is the bulletproof variant). But the modern verdict matters more than the mechanics: **hand-rolled singletons are mostly obsolete.** The `getInstance()` global is hidden coupling — untestable (can't substitute a mock; the static-mutable-state warnings from Unit 2 apply in full) — and Spring already gives every bean singleton *scope* with none of the costs: one instance, injected where declared, swappable in tests. Recognize the pattern; in a Spring codebase, express it as a bean.

---

## Factory — Deciding Which Class to Construct

**Problem:** the caller knows *what it needs* ("a parser for this file") but choosing the *concrete class* requires logic that shouldn't live at every call site. **Solution:** centralize creation behind a method that returns the abstraction:

```java
public final class ParserFactory {

    public static Parser forFile(Path file) {
        String name = file.getFileName().toString().toLowerCase();
        if (name.endsWith(".csv"))  return new CsvParser();
        if (name.endsWith(".json")) return new JsonParser();
        if (name.endsWith(".xml"))  return new XmlParser();
        throw new IllegalArgumentException("unsupported format: " + name);
    }
}

Parser parser = ParserFactory.forFile(path);      // caller sees only the interface
List<Record> records = parser.parse(path);        // polymorphism from here on
```

*A static factory method: selection logic written once, callers coupled only to `Parser`.*

This is polymorphism's missing bookend — Unit 3 showed `instanceof` chains *consuming* types as a smell; the factory is where the one legitimate type-selection lives, quarantined. Adding a format touches the factory alone. Variants on the theme: **static factory methods** on the type itself (`List.of`, `Optional.empty`, `Duration.ofSeconds` — better-named, cache-capable alternatives to constructors), and the heavier **Factory Method / Abstract Factory** patterns (subclasses or families deciding creation) that appear mainly inside frameworks.

---

## Builder — Constructing Complex Objects Readably

**Problem:** an object with many parameters, most optional — the telescoping-constructor mess the Parameters lesson warned about (`new Report(title, null, true, false, 3, null, PDF)` — which boolean was what?). **Solution:** a companion object that accumulates settings by name, then builds:

```java
Report report = Report.builder()
        .title("Q3 Sales")
        .format(Format.PDF)
        .includeCharts(true)
        .maxRows(500)
        .build();                       // validation happens here, once, atomically
```

*The builder call site: every value labeled, optionals omitted, the result immutable.*

```java
public class Report {
    private final String title;         // all final — built objects don't change
    private final Format format;
    private final boolean includeCharts;
    private final int maxRows;

    private Report(Builder b) {         // private: the builder is the only door
        this.title = b.title; this.format = b.format;
        this.includeCharts = b.includeCharts; this.maxRows = b.maxRows;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String title;
        private Format format = Format.HTML;        // defaults live here
        private boolean includeCharts = false;
        private int maxRows = 1000;

        public Builder title(String t) { this.title = t; return this; }     // return this:
        public Builder format(Format f) { this.format = f; return this; }   // the fluent chaining
        public Builder includeCharts(boolean b) { this.includeCharts = b; return this; }
        public Builder maxRows(int n) { this.maxRows = n; return this; }

        public Report build() {
            if (title == null || title.isBlank())
                throw new IllegalStateException("title is required");        // fail fast, complete picture
            return new Report(this);
        }
    }
}
```

*The implementation: a mutable builder, `return this` chaining (the `this` lesson's fluent idiom), validation in `build`, an immutable product.*

We've consumed builders all along — `StringBuilder`, `PageRequest.of`... and Spring's own `ResponseEntity.created(...).body(...)`. Reach for one at roughly *four-plus parameters or two-plus optionals*; below that, constructors and records stay simpler (records handle the immutability, builders the ergonomics — large records often get a builder too, and libraries like Lombok generate the whole thing from one annotation).

---

## The Family Resemblance

All three patterns take construction away from ad-hoc `new` at call sites and give it a managed home — a class's own instance, a selection method, a staged assembler. That's also a fair one-line description of Spring Core: the container as the application-wide creational pattern. Next: the **structural** patterns, which compose existing objects into new shapes without touching their code.
