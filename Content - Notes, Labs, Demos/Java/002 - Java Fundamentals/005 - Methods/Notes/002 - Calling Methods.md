# Calling Methods

Defining a method declares what *can* happen; a **call** makes it happen. We covered the control-flow mechanics in the Jumps lesson — the round trip, the stack frame. This lesson is about the calling syntax in its several forms, which all reduce to one question: *what is the method being called on?*

---

## Calling Within the Same Class

A method in the same class is called by bare name:

```java
public class Billing {
    public static void main(String[] args) {
        int total = applyDiscount(2000, 15);     // same class — just the name
        System.out.println(total);
    }

    static int applyDiscount(int price, int percent) {
        return price - (price * percent / 100);
    }
}
```

*Same-class calls need no prefix — name, parentheses, arguments.*

The **arguments** — the actual values in the parentheses — are evaluated first (left to right, including any nested calls), then copied into the method's parameters. Argument count and types must match a signature, or the code doesn't compile.

---

## Calling Static Methods: `ClassName.method(...)`

A `static` method belongs to a class, so from outside that class, the class name qualifies the call:

```java
double root = Math.sqrt(144.0);          // sqrt is static in Math
int bigger = Math.max(7, 12);
int n = Integer.parseInt("42");          // the wrapper utilities — also static
long now = System.currentTimeMillis();
```

*Static calls read `ClassName.method(...)` — no object required, just the class.*

We've been using these all along; the pattern to notice is that nothing is "constructed" first. `Math.sqrt` needs no `Math` object — the method is a pure function hanging off the class name.

---

## Calling Instance Methods: `reference.method(...)`

An **instance method** runs against a specific object, so the call needs a reference to that object on the left of the dot:

```java
String name = "ada lovelace";
int len = name.length();                 // called ON the string that name refers to

StringBuilder sb = new StringBuilder();
sb.append("Hello");                      // called ON the builder object
```

*Instance calls read `reference.method(...)` — the object left of the dot is the one acted upon.*

The object matters: `name.length()` and `otherName.length()` run the same code against different data. This object-receives-the-call model is the core of the next topic; the syntax is worth being fluent in now since every `String` and array operation already uses it. One hazard carries over from earlier lessons: if the reference is `null`, any instance call through it throws a `NullPointerException` — there's no object to receive the call.

---

## Chaining

When a method returns an object, another call can hang directly off the result — **method chaining**:

```java
String cleaned = input.strip().toLowerCase().replace(" ", "-");

sb.append("a").append("b").append("c");   // builder methods return the builder itself
```

*Each call runs left to right, and the next call applies to the previous call's result.*

The first line works because each `String` method returns a new string (immutability in action); the second because `StringBuilder.append` deliberately returns `this` to invite chaining — a design called a *fluent API*. Chains read well up to a point; past three or four links, or when any link might return `null`, intermediate variables with names beat cleverness.

---

## Using the Result (or Not)

A call to a non-`void` method is an **expression** — it has a value, usable anywhere a value fits:

```java
int max = Math.max(a, b);                          // assigned
System.out.println(Math.max(a, b));                // passed as an argument
if (list.isEmpty()) { ... }                        // tested
return applyDiscount(price, 10);                   // returned onward
```

*A method call with a return value slots in wherever that value's type is welcome.*

Java also allows *discarding* the result — calling a value-returning method as a bare statement. Occasionally that's intended (`list.remove(item)` returns a boolean many callers ignore); with immutable types it's almost always the capture-the-result bug from the Strings topic: `name.toUpperCase();` on its own line does nothing useful. The IDE's "result of call ignored" warning has saved careers; leave it enabled.

A `void` method, by contrast, *is only* a statement — `System.out.println("hi")` can't be assigned or nested, because there's no value to use.
