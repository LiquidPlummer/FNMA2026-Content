# Lab: Program to the Contract

In this lab we build both of Java's abstraction tools and feel where each fits: an `Exporter` **interface** with swappable implementations (plus a `default` method), and a `TextReport` **abstract class** whose `final` template method locks a skeleton around subclass-supplied steps. The finale is the choosing table from the notes, filled in against code we just wrote.

---

## Prerequisites

| Software | Required Version |
|---|---|
| JDK (Temurin or any OpenJDK build) | 21 (LTS) |
| Apache Maven | 3.9.x |

From this lab's folder:

```console
mvn -q compile exec:java
```

Starters: [ContractLab.java](src/main/java/com/curriculum/labs/ContractLab.java) (the driver, with markers) and [ReportData.java](src/main/java/com/curriculum/labs/ReportData.java) (the data — note its `getRows()` already practices the defensive-copy idiom from Fundamentals). Everything else is ours to create.

---

## Part 1 — An interface and its first implementation

The requirement: reports must export to multiple formats, and the set of formats will grow. We start with the contract — *what* an exporter does, no how:

```java
package com.curriculum.labs;

public interface Exporter {
    String export(ReportData report);
}
```

*The whole interface: one capability, stated as a signature. New file `Exporter.java`.*

Then the first implementor, new file `CsvExporter.java`:

```java
package com.curriculum.labs;

public class CsvExporter implements Exporter {
    @Override
    public String export(ReportData report) {
        StringBuilder out = new StringBuilder("item,count\n");
        for (String[] row : report.getRows()) {
            out.append(String.join(",", row)).append('\n');
        }
        return out.toString();
    }
}
```

*`implements` is the promise; `@Override` confirms we're fulfilling it (delete the method briefly and read the compile error — the contract is enforced, not suggested).*

At **`[MARKER 1]`**: `System.out.println(new CsvExporter().export(data));` — run it.

---

## Part 2 — The swap: clients that never learn the concrete class

Now the second format — create `JsonExporter.java` yourself: same interface, body builds `[{"item": "widget", "count": 120}, ...]` (a `StringBuilder` and a loop; don't chase perfect JSON escaping). Then the important part — a client typed against the *interface*. At **`[MARKER 4]`**:

```java
static void deliver(ReportData data, Exporter exporter) {
    System.out.println("--- delivering " + data.getTitle() + " ---");
    System.out.println(exporter.export(data));
}
```

*The client knows the contract, not the implementations — `Exporter` appears; `CsvExporter` does not.*

At **`[MARKER 2]`**: `deliver(data, new CsvExporter());` — run, then change *only the constructor call* to `new JsonExporter()` and run again. That one-line swap is the entire argument for programming to interfaces: the client, the method signature, and every future caller stayed untouched while the behavior changed completely. (Where the concrete choice should live — config, a factory, injection — is a Java Advanced story; the seam we just built is what makes all of those possible.)

---

## Part 3 — A default method: evolve the contract without breaking it

Suppose every exporter should also report its output's size — useful, derivable, and adding an abstract method would break `CsvExporter` and `JsonExporter` both. A **`default` method** adds it with an implementation built on the existing contract. In `Exporter.java`:

```java
default String exportWithFooter(ReportData report) {
    String body = export(report);
    return body + "-- " + body.length() + " chars, "
            + report.getRows().size() + " rows --\n";
}
```

*A default: defined once on the interface in terms of the abstract method — both implementors inherit it instantly, unmodified.*

Call `exportWithFooter` from `deliver` instead of `export` and run: both formats now carry the footer, and neither implementation file was touched. That's the default method's actual job — interface evolution — not a backdoor for putting logic on interfaces.

---

## Part 4 — The abstract class: a skeleton with holes

Different tool, different problem: all our *printed* reports share a fixed structure — banner, body, footer — and only the body varies. Shared structure plus shared state is abstract-class territory. New file `TextReport.java`:

```java
package com.curriculum.labs;

public abstract class TextReport {
    private final String title;

    protected TextReport(String title) {
        this.title = title;
    }

    public final String render() {                 // final: the skeleton is not negotiable
        return "==== " + title + " ====\n"
                + body()
                + "---- end of " + title + " ----\n";
    }

    protected abstract String body();              // the hole each subclass fills
}
```

*The template method pattern: `render` is `final` (subclasses can't rearrange the skeleton), `body` is abstract (they must fill the hole), and the constructor is `protected` — only subclasses construct.*

Try `new TextReport("x")` in `main` — the compiler refuses: abstract classes can't be instantiated; they exist to be completed. Now complete it twice. Create `SalesReport` (constructor takes a `ReportData`, body loops its rows into `name: count` lines) and `StaffReport` (hard-code two or three lines of fake staffing text — the *variety* is the point, not the content). At **`[MARKER 3]`**, render both. Note what each subclass did *not* write: banners, footers, title handling — inherited machinery, exactly the code that stays consistent because no subclass can touch it.

---

## The Choosing Table, Filled In

The notes' table, now with our own classes as the entries — worth writing into a comment at the bottom of `ContractLab`:

| | `Exporter` (interface) | `TextReport` (abstract class) |
|---|---|---|
| Models | a capability: "can export" | a half-built thing: "is a report" |
| State | none — pure contract | `title` field + constructor |
| Implementors | could be anything, anywhere | a tight family of report types |
| Flexibility | a class could implement several | one parent, period |

The default rule from the notes held up here: we *started* with the interface, and reached for the abstract class only when genuine shared state and skeleton code appeared.

---

## Exercises

1. **The third format.** Implement `MarkdownExporter` (a `| item | count |` table) against the `Exporter` contract *without opening `CsvExporter` or `JsonExporter`* — the interface alone should tell you everything you need. Then add it to a `deliver` call. If you had to peek, note what the interface failed to communicate.

2. **Retrofit.** Define a `Describable` interface with one method `String describe()`. Retrofit it onto **both** `ReportData` and one exporter — two classes with nothing else in common — and write a loop over a `Describable[]` containing both. This is the trick class hierarchies can't do; say why in a comment.

3. **Skeleton, extended.** Add a third `TextReport` subclass, `InventoryReport`, and then a new *optional* hook to the skeleton: a `protected String footnote()` returning `""` by default (not abstract), included by `render`. Only `InventoryReport` overrides it. Compare this move with Part 3's default method — same idea, different tool; one sentence on when each applies.

4. **Three scenarios, one decision each.** Interface, abstract class, or plain class — pick and defend in two sentences per case:
   a. Five payment providers must all support `charge` and `refund`; they share no code.
   b. Four import jobs all do open-validate-process-log with only `process` differing.
   c. A `Money` type with amount, currency, and arithmetic.
