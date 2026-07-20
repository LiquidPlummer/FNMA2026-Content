# Java Keywords Reference

## Data Types

| Keyword | Description |
|---|---|
| `boolean` | Declares a boolean variable (`true` or `false`) |
| `byte` | Declares an 8-bit integer variable |
| `short` | Declares a 16-bit integer variable |
| `int` | Declares a 32-bit integer variable |
| `long` | Declares a 64-bit integer variable |
| `float` | Declares a 32-bit floating-point variable |
| `double` | Declares a 64-bit floating-point variable |
| `char` | Declares a single 16-bit Unicode character variable |
| `void` | Indicates a method returns no value |
| `var` | Local variable type inference — type is inferred from the assigned value (Java 10+) |

---

## Access Modifiers

| Keyword | Description |
|---|---|
| `public` | Accessible from anywhere |
| `private` | Accessible only within the declaring class |
| `protected` | Accessible within the package and by subclasses |
| *(none)* | **Package-private** — the default when no modifier is specified; accessible only within the same package |

---

## Class & Object

| Keyword | Description |
|---|---|
| `class` | Declares a class |
| `interface` | Declares an interface |
| `enum` | Declares an enumerated type |
| `record` | Declares a record — an immutable data carrier class (Java 16+) |
| `extends` | Indicates a class inherits from a superclass |
| `implements` | Indicates a class implements an interface |
| `abstract` | Declares an abstract class or method (no implementation) |
| `new` | Creates a new object instance |
| `this` | Refers to the current object instance |
| `super` | Refers to the parent class |
| `instanceof` | Tests whether an object is an instance of a class or interface |
| `static` | Belongs to the class itself, not to instances |
| `final` | Declares a constant, prevents method overriding or class inheritance |

---

## Control Flow

| Keyword | Description |
|---|---|
| `if` | Begins a conditional statement |
| `else` | Alternative branch of an `if` statement |
| `switch` | Begins a switch statement |
| `case` | Defines a branch in a switch statement |
| `default` | Default branch in a switch statement; also default methods in interfaces |
| `for` | Begins a for loop |
| `while` | Begins a while loop |
| `do` | Begins a do-while loop |
| `break` | Exits a loop or switch statement |
| `continue` | Skips to the next iteration of a loop |
| `return` | Exits a method, optionally returning a value |

---

## Exception Handling

| Keyword | Description |
|---|---|
| `try` | Begins a block of code to watch for exceptions |
| `catch` | Handles an exception thrown in a `try` block |
| `finally` | Block that always executes after `try`/`catch`, regardless of outcome |
| `throw` | Throws an exception explicitly |
| `throws` | Declares that a method may throw an exception |

---

## Packages & Imports

| Keyword | Description |
|---|---|
| `package` | Declares the package a class belongs to |
| `import` | Imports a class or package for use in the file |

---

## Concurrency

| Keyword | Description |
|---|---|
| `synchronized` | Restricts a method or block to one thread at a time |
| `volatile` | Indicates a variable may be modified by multiple threads |

---

## Other

| Keyword | Description |
|---|---|
| `assert` | Tests a condition during development; throws `AssertionError` if false |
| `transient` | Excludes a field from serialization |