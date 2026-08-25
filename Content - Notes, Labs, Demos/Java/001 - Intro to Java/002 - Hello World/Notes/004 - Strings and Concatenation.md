# Strings and Concatenation

Text in Java is handled by the `String` type. Our first programs are mostly about producing text output, so strings and the `+` operator for joining them are some of the first tools we'll reach for.

---

## The `String` Type in Java

A **`String`** is a sequence of characters — text of any length, from empty to enormous. Unlike `int` or `double`, which are primitive types, `String` is a class from the standard library (`java.lang.String`), so every string is an object. We can declare and assign string variables just like any other type:

```java
String greeting = "Hello";
String empty = "";
String sentence = "Java strings can hold any text, including spaces and punctuation.";
```

Because strings are objects, they come with built-in methods we can call using the dot operator:

```java
String name = "Alice";

name.length();          // 5 — number of characters
name.toUpperCase();     // "ALICE"
name.charAt(0);         // 'A' — character at index 0
name.contains("lic");   // true
```

One property worth knowing from the start: strings in Java are **immutable** — once created, a string never changes. Methods like `toUpperCase()` don't modify the original; they return a *new* string. If we want to keep the result, we have to assign it:

```java
String name = "alice";
name.toUpperCase();              // result is discarded — name is still "alice"
name = name.toUpperCase();       // name is now "ALICE"
```

---

## String Literals

A **string literal** is text written directly in source code, enclosed in double quotes:

```java
String message = "Hello, World!";
```

Java is strict about quote characters: double quotes (`"`) make a `String`, while single quotes (`'`) make a single `char`. Writing `'Hello'` is a compile error — single quotes can hold exactly one character.

Some characters can't be typed directly inside quotes, so we write them with a backslash **escape sequence**:

```java
String quoted = "She said \"hello\" to everyone.";   // embedded double quotes
String path = "C:\\Users\\alice";                    // a literal backslash
String multilineish = "Line one\nLine two";          // \n is a newline
String columns = "Name\tAge";                        // \t is a tab
```

---

## Concatenation with `+`

**Concatenation** means joining strings end to end, and in Java the `+` operator does it. When either side of `+` is a string, the result is a new string:

```java
String first = "Hello";
String second = "World";

String combined = first + ", " + second + "!";   // "Hello, World!"
System.out.println(combined);
```

Concatenation chains read left to right, and we can build strings up across multiple statements with `+=`:

```java
String report = "Results: ";
report += "passed";          // "Results: passed"
report += ", 0 failures";    // "Results: passed, 0 failures"
```

Notice that `+` does not insert spaces — `"Hello" + "World"` is `"HelloWorld"`. Forgetting the space inside the quotes is one of the most common early mistakes.

---

## Mixing Types in Concatenation

The `+` operator can combine a string with *any* other type. When one operand is a string, Java converts the other operand to its text form automatically:

```java
int count = 42;
double price = 9.99;
boolean active = true;

System.out.println("Count: " + count);       // Count: 42
System.out.println("Price: " + price);       // Price: 9.99
System.out.println("Active: " + active);     // Active: true
```

This is what makes `println` output so convenient — we can mix labels and values in one expression without converting anything ourselves.

There's a gotcha lurking here, though. The `+` operator means *addition* for numbers and *concatenation* for strings, and Java evaluates left to right. The result depends on which meaning applies first:

```java
int a = 1;
int b = 2;

System.out.println("Sum: " + a + b);     // Sum: 12  — not 3!
System.out.println("Sum: " + (a + b));   // Sum: 3
System.out.println(a + b + " total");    // 3 total — addition happens first here
```

In the first line, `"Sum: " + a` produces the string `"Sum: 1"`, and then `+ b` concatenates `"2"` onto it. Parentheses force the numeric addition to happen before any string gets involved. Whenever we're concatenating numbers and the result looks wrong, this left-to-right rule is the first thing to check.
