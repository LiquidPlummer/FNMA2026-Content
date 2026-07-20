# Lab: Hierarchy & Dispatch

In this lab we build an inheritance hierarchy — `Employee` with three subclasses — and put polymorphism to work: overriding with `@Override`, constructor chaining with `super(...)`, one payroll loop that dispatches to three different pay calculations, and a before/after moment where an `instanceof` ladder gets deleted in favor of dynamic dispatch. One legitimate downcast closes it out.

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

The starter is just [PayrollLab.java](src/main/java/com/curriculum/labs/PayrollLab.java) with markers — every class in the hierarchy is ours to create, one file each, in `src/main/java/com/curriculum/labs/`.

---

## Part 1 — The base class

We'll create `Employee.java` — the general case every specific kind of employee shares:

```java
package com.curriculum.labs;

public class Employee {
    private final String id;
    private final String name;

    public Employee(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public double monthlyPay() {
        return 0.0;            // the base has no pay policy — subclasses will
    }

    public String getId() { return id; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}
```

*The base: private final state, a constructor that establishes it, and a `monthlyPay` the subclasses will replace.*

Two deliberate decisions to notice as we type. The fields are **`private`, not `protected`** — subclasses will reach them through the getters, keeping the encapsulation wall intact even inside the family (the notes' advice: `protected` fields are a smaller wall, not a better one). And `monthlyPay` returning `0.0` is a placeholder with a smell — a base class with no sensible default is hinting it wants to be *abstract*, which is exactly where the next lesson's lab takes it; today we live with the smell and note it.

At **`[MARKER 1]`**, construct and print one: `System.out.println(new Employee("E-000", "Placeholder Person") + " earns " + new Employee("E-000", "Placeholder Person").monthlyPay());` — or tidier with a variable. Run it.

---

## Part 2 — The first subclass, and the typo that `@Override` catches

Create `SalariedEmployee.java`:

```java
package com.curriculum.labs;

public class SalariedEmployee extends Employee {
    private final double annualSalary;

    public SalariedEmployee(String id, String name, double annualSalary) {
        super(id, name);                   // parent constructs its part first
        this.annualSalary = annualSalary;
    }

    @Override
    public double monthlyPay() {
        return annualSalary / 12;
    }
}
```

*`extends` + `super(...)`: the subclass adds one field and replaces one behavior; everything else — getters, `toString` — is inherited.*

Before running, one experiment worth thirty seconds for a career of protection. Change the method name to `monthlypay` (lowercase p) and keep the `@Override`. Compile (`mvn -q compile`):

```
error: method does not override or implement a method from a supertype
```

*Without `@Override`, that typo would have silently created a NEW method — and every caller of `monthlyPay()` would get the base's 0.0. With it, the mistake can't survive compilation.*

Fix the name back. In `main`, construct a `SalariedEmployee("E-001", "Ada", 96_000)` and print name and pay — 8000.0, the override in action.

---

## Part 3 — Two more shapes of pay

Two more subclasses, same pattern — we write these with less hand-holding:

- **`HourlyEmployee`** — extra fields `hourlyRate` and `hoursPerMonth`; `monthlyPay()` returns their product.
- **`ContractEmployee`** — extra fields `monthlyRate` and `endDate` (a `String` like `"2026-12-31"` is fine for this lab), plus a getter `getEndDate()`; `monthlyPay()` returns the flat rate.

Both: constructor chains to `super(id, name)`, `@Override` on the pay method. Construct one of each in `main` and print their pays to confirm three different formulas answer the same method call.

---

## Part 4 — The payroll loop: conditionals vs. dispatch

Now the payoff. At **`[MARKER 2]`**, build the roster:

```java
Employee[] roster = {
    new SalariedEmployee("E-001", "Ada", 96_000),
    new HourlyEmployee("E-002", "Grace", 45.0, 160),
    new ContractEmployee("E-003", "Edsger", 9_500, "2026-12-31"),
    new SalariedEmployee("E-004", "Barbara", 120_000),
};
```

*Four objects, three classes, one array type — every element IS an Employee.*

First, the loop a pre-OOP instinct writes — type it in full; feeling the shape matters:

```java
for (Employee e : roster) {
    double pay;
    if (e instanceof SalariedEmployee s) {
        pay = s.monthlyPay();
    } else if (e instanceof HourlyEmployee h) {
        pay = h.monthlyPay();
    } else if (e instanceof ContractEmployee c) {
        pay = c.monthlyPay();
    } else {
        pay = e.monthlyPay();
    }
    System.out.printf("%-25s %10.2f%n", e, pay);
}
```

*The ladder: it works, and every branch does the identical thing — ask the object for its pay.*

Run it. Then delete the entire ladder and replace the body with:

```java
for (Employee e : roster) {
    System.out.printf("%-25s %10.2f%n", e, e.monthlyPay());
}
```

*Dynamic dispatch: the object's actual class picks the method at runtime — the ladder was doing, badly, what the language does for free.*

Same output. Now the question that separates the two versions: add a fourth employee type — which version needs editing? The ladder needs a new branch *everywhere it was copy-pasted*; the dispatch loop needs nothing. That's the open/closed principle as a felt experience, and it's Exercise 1.

---

## Part 5 — The one honest downcast

Reports sometimes genuinely need subtype-specific data. At **`[MARKER 3]`**, we'll list contract end dates — `getEndDate()` exists only on `ContractEmployee`, so this is the legitimate use of pattern matching:

```java
System.out.println("--- Contract end dates ---");
for (Employee e : roster) {
    if (e instanceof ContractEmployee c) {
        System.out.println(c.getName() + " ends " + c.getEndDate());
    }
}
```

*Test-and-bind in one step: inside the block, `c` is already typed as ContractEmployee — no separate cast line.*

The design instinct to carry away: this is fine *because end dates are genuinely contractor-only*. If we caught ourselves writing `instanceof` to pick pay formulas again, that would be Part 4's ladder sneaking back — the question to ask is always "could this be an overridden method instead?"

---

## Exercises

1. **The fourth type.** Add `CommissionEmployee` (base monthly amount plus `commissionRate * salesThisMonth`). Requirement: the payroll loop in Part 4 must not change *at all* — only the roster line adding the new hire. Confirm by diff.

2. **Wrap, don't extend.** The auditors want every pay calculation logged. Create `AuditedEmployee` that *wraps* any `Employee` (composition: holds one, delegates `monthlyPay()`, printing a log line around the call) rather than extending a specific subclass. Add one wrapped employee to the roster — the loop, again, must not change. In a comment, state why extending `Employee` for this job would have been worse.

3. **equals for the family.** Give `Employee` a proper `equals`/`hashCode` based on `id` (the notes' pattern, `instanceof` form). Then answer in a comment: should a `SalariedEmployee` with id `E-001` equal a `ContractEmployee` with id `E-001`? There are two defensible answers — pick one and defend it in two sentences.

4. **Break substitutability on purpose.** Write (in a comment, not real code) a subclass override of `monthlyPay()` that would technically compile but violate what callers of `Employee` reasonably assume — then state the rule it breaks and why the compiler can't catch this class of error.
