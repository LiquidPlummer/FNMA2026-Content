# Inspecting Fields, Methods & Constructors

The `Class` object holds the door; behind it are the members. Reflection represents each kind as an object — **`Field`**, **`Method`**, **`Constructor`** (package `java.lang.reflect`) — that can be enumerated, inspected, and *used*: fields read and written, methods invoked, constructors called. This is the layer where "framework magic" turns out to be plain, if verbose, code.

---

## Enumerating Members

Two families of accessors, differing in scope — the distinction that trips everyone:

```java
Class<?> c = BankAccount.class;

Method[] pub = c.getMethods();             // PUBLIC methods — including inherited ones
Method[] own = c.getDeclaredMethods();     // ALL declared here — private too, but NOT inherited

Field f = c.getDeclaredField("balance");           // one, by name
Method m = c.getMethod("deposit", double.class);   // by name + parameter types (the signature!)
Constructor<?> ctor = c.getConstructor(String.class, double.class);
```

*`getX` = public + inherited; `getDeclaredX` = everything, this class only. Lookups take the signature because of overloading.*

Each member object then answers the questions the compiler normally answers — name, type, parameters, modifiers:

```java
for (Method method : c.getDeclaredMethods()) {
    System.out.printf("%s %s(%s)%n",
        method.getReturnType().getSimpleName(),
        method.getName(),
        Arrays.toString(method.getParameterTypes()));
    if (Modifier.isPrivate(method.getModifiers())) { ... }
}
```

*Walking a class's API programmatically — the loop at the heart of every serializer and test runner.*

---

## Invoking and Accessing

Member objects are live handles, not just descriptions:

```java
BankAccount acct = new BankAccount("Ada", 100.0);

// Invoke a method: method.invoke(target, args...)
Method deposit = c.getMethod("deposit", double.class);
deposit.invoke(acct, 50.0);                        // acct.deposit(50.0), spelled reflectively

// Read a field — a private one, at that
Field balance = c.getDeclaredField("balance");
balance.setAccessible(true);                       // disarm the private check
double value = (double) balance.get(acct);         // 150.0

// Construct
Constructor<BankAccount> ctor2 = BankAccount.class.getConstructor(String.class, double.class);
BankAccount fresh = ctor2.newInstance("Grace", 500.0);
```

*The three verbs of reflection: `invoke` on methods, `get`/`set` on fields, `newInstance` on constructors.*

Mechanics worth noting. Arguments and returns travel as `Object` — primitives box and unbox automatically (`50.0` boxes to `Double`; the wrapper classes, load-bearing again). A static method invokes with `null` as the target. Exceptions thrown *by the invoked method* arrive wrapped in `InvocationTargetException` — the real failure is `getCause()`, a fact that saves hours reading framework stack traces.

**`setAccessible(true)`** is the encapsulation bypass previewed twice now. It's how test frameworks reach private state and how JPA (Spring Data topic) populates private entity fields without setters. Two honest caveats: the module system (Java 9+) can refuse it — JDK internals are off-limits by default, and libraries increasingly declare what they open — and in *our* code, needing it for anything but tests or serialization is a design alarm, not a technique.

---

## The Payoff Pattern: Generic Object Processing

Everything above assembles into the loop that powers serializers, mappers, and validators — code that processes objects *it has never seen*:

```java
static Map<String, Object> toMap(Object obj) throws IllegalAccessException {
    Map<String, Object> out = new LinkedHashMap<>();
    for (Field field : obj.getClass().getDeclaredFields()) {
        if (Modifier.isStatic(field.getModifiers())) continue;   // instance state only
        field.setAccessible(true);
        out.put(field.getName(), field.get(obj));
    }
    return out;
}

toMap(new BankAccount("Ada", 150.0));      // {owner=Ada, balance=150.0}
```

*A ten-line universal serializer: any object in, name→value map out — the skeleton of every JSON library.*

Jackson's `ObjectMapper`, JPA's entity hydration, Spring's property binding — each is this loop, industrialized: enumerate members, filter by modifier or annotation, read or write values. Which surfaces the missing ingredient: real frameworks don't process *every* field or method — they process the ones marked `@JsonProperty`, `@Id`, `@Test`, `@Autowired`. Reading those markers reflectively is the final piece of the machinery: annotations at runtime, next.
