# Scope

**Scope** is the region of code where a variable can be referenced by name. Java determines scope entirely from where the variable is *declared* — and the core rule is compact: **a variable is visible from its declaration to the end of the block (the `{ ... }` pair) that contains it.** Everything else in this lesson is that rule applied to the places a declaration can appear.

---

## Block Scope

Any pair of braces forms a block, and a variable declared inside is invisible outside:

```java
void process() {
    int total = 0;               // scope: rest of the method body

    if (total == 0) {
        String message = "empty";     // scope: this if-block only
        System.out.println(message);
    }

    // System.out.println(message);  // compile error: cannot find symbol
}
```

*`message` lives only inside the `if` block; referencing it afterward doesn't compile.*

Blocks nest, and inner blocks see the variables of every enclosing block — visibility flows inward, never outward. Once execution leaves a block, its local variables are gone; there is no way to "get them back," and the next entry into the block starts fresh.

---

## Method Scope: Locals and Parameters

Variables declared in a method body are **local variables**, and a method's **parameters** are locals too — in scope for the entire body, as if declared on the first line:

```java
int applyDiscount(int price, int percent) {   // price, percent: whole body
    int discount = price * percent / 100;     // discount: from here down
    return price - discount;
}
```

*Parameters and locals are both method-scoped; nothing here survives after the method returns.*

Locals in different methods never collide — a `discount` in one method has no relationship to a `discount` in another. That isolation is what makes methods independently understandable.

---

## Loop Scope

A variable declared in a `for` header belongs to the loop:

```java
for (int i = 0; i < 10; i++) {
    System.out.println(i);
}
// System.out.println(i);   // compile error: i does not exist here
```

*The loop index `i` is scoped to the loop itself — header and body.*

This is a feature, not a limitation: the index can't leak into later code, and the next loop can reuse the name `i` without conflict. If the final value of a counter is genuinely needed after the loop, declare it before the loop — deliberately widening its scope.

---

## Class Scope: Fields

Variables declared directly in a class — **fields** — are scoped to the whole class: every method, constructor, and initializer in the class can use them, regardless of declaration order.

```java
public class Counter {
    private int count;               // field: visible throughout the class

    void increment() {
        count++;                     // no local declaration needed
    }

    int current() {
        return count;
    }
}
```

*A field is shared state: both methods read and write the same `count`.*

Fields differ from locals in lifetime as well as scope. An **instance field** lives as long as the object it belongs to; a **`static` field** belongs to the class itself — one copy shared by all instances, alive for the life of the program. (How far a field is visible *outside* its class is a separate concern — access modifiers like `private` and `public` — covered in the Classes & Objects topic.)

---

## Shadowing

When an inner declaration reuses the name of a field, the inner one wins within its scope — it **shadows** the field. The most common case is a constructor or setter whose parameter is named after the field, and the standard resolution is the **`this`** keyword, which always refers to the current object's fields:

```java
public class User {
    private String name;

    public User(String name) {    // parameter 'name' shadows the field
        this.name = name;         // this.name = field, name = parameter
    }
}
```

*Inside the constructor, `name` means the parameter; `this.name` reaches the shadowed field.*

Java does draw one hard line: a local variable can't shadow another *local* in an enclosing block of the same method — that's a compile error. Shadowing is only legal (and only tolerable) across the field/local boundary, and outside the constructor/setter idiom it's best avoided: two meanings for one name in one screen of code is a bug invitation.

---

## No Globals — and Why Small Scope Wins

Java has no global variables. The widest scope available is a `public static` field, which still lives inside a class and is referenced through it (`Math.PI`, `Integer.MAX_VALUE`). That design nudges toward the habit worth adopting deliberately: **declare every variable in the smallest scope that works, as close to first use as possible.** The payoff is practical — anyone reading the code can see a variable's entire life without scrolling, and the compiler can catch more mistakes because names simply don't exist where they shouldn't.
