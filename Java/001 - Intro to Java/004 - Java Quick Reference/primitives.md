# Java Primitive Data Types Reference

## Primitive Data Types

| Type | Size | Description | Example |
|---|---|---|---|
| `byte` | 8-bit | Integer, -128 to 127 | `byte b = 100;` |
| `short` | 16-bit | Integer, -32,768 to 32,767 | `short s = 1000;` |
| `int` | 32-bit | Integer, ~-2B to 2B | `int i = 42;` |
| `long` | 64-bit | Large integer | `long l = 123456789L;` |
| `float` | 32-bit | Decimal number | `float f = 3.14f;` |
| `double` | 64-bit | Higher precision decimal | `double d = 3.14159;` |
| `char` | 16-bit | Single character | `char c = 'A';` |
| `boolean` | — | `true` or `false` | `boolean flag = true;` |

---

## Wrapper Classes

Each primitive has a corresponding wrapper class in `java.lang`. These allow primitives to be used where objects are required (e.g. in collections).

| Primitive | Wrapper Class |
|---|---|
| `byte` | `Byte` |
| `short` | `Short` |
| `int` | `Integer` |
| `long` | `Long` |
| `float` | `Float` |
| `double` | `Double` |
| `char` | `Character` |
| `boolean` | `Boolean` |

Java automatically converts between primitives and their wrapper classes as needed — this is called **autoboxing** (primitive → wrapper) and **unboxing** (wrapper → primitive).

---

## String

`String` is not a primitive — it is a class. It represents a sequence of characters and is one of the most commonly used types in Java.

```java
String name = "Alice";
```

Strings are **immutable**: once created, their value cannot be changed. Operations that appear to modify a string actually produce a new one.