# Classes and Objects

Python supports classes, inheritance, and methods, and a Java developer will recognize the overall shape immediately. The details are looser in almost every direction. There are no field declarations, no access modifiers, and no implicit `this`; attributes spring into existence when assigned; and one decorator can generate the constructor, equality, and string representation that Java needs a record or Lombok to produce.

---

## Defining a Class

A class is defined with `class`, a name, and an indented body. Methods are functions defined inside it:

```python
class Account:
    def __init__(self, owner, balance=0):
        self.owner = owner
        self.balance = balance

    def deposit(self, amount):
        self.balance += amount
        return self.balance

acct = Account("Ada", 100)
acct.deposit(50)
print(acct.owner, acct.balance)     # Ada 150
```

*A class with an initializer and one method, then an instance created and used.*

- **`__init__`** sets up a new instance. It's the closest thing to a constructor: by the time it runs, the object already exists, and its job is to give it attributes. It returns nothing.
- **Instances are created by calling the class** like a function — there's no `new` keyword.
- Class names use `PascalCase`, per PEP 8.

---

## `self` Is Explicit

Every instance method takes the instance as its first parameter, named **`self`** by convention. Python passes it automatically when a method is called through an instance:

```python
acct.deposit(50)
Account.deposit(acct, 50)       # what the line above actually does
```

*Calling a method through an instance is shorthand for calling the class's function with the instance as the first argument.*

Because nothing is implicit, every attribute access inside a method goes through `self`. Writing `balance += amount` instead of `self.balance += amount` refers to a local name, not the attribute.

Forgetting `self` in the parameter list produces a confusing-looking error:

```python
class Greeter:
    def greet():
        return "hi"

Greeter().greet()
# TypeError: ... greet() takes 0 positional arguments but 1 was given
```

*The instance is passed automatically, but the method has no parameter to receive it.*

**Contrast:** Java's `this` is implicit and optional — `balance += amount` reads the field without it. Python requires `self` in both the method signature and every attribute access.

---

## No Field Declarations

A Python class doesn't list its fields. An attribute exists once something **assigns** it, and not before:

```python
class Order:
    def __init__(self, order_id):
        self.order_id = order_id

    def ship(self):
        self.shipped_on = "2026-03-14"

order = Order(7)
print(order.shipped_on)     # AttributeError: 'Order' object has no attribute 'shipped_on'

order.ship()
print(order.shipped_on)     # 2026-03-14
```

*`shipped_on` doesn't exist until `ship()` assigns it.*

The convention that keeps this manageable: **assign every attribute in `__init__`**, using `None` for values that aren't known yet. Then `__init__` reads as the class's field list.

**Contrast:** a Java class declares its fields up front, and the compiler rejects any reference to one that doesn't exist. In Python, the shape of an object is whatever has been assigned to it so far.

---

## Instance Attributes vs Class Attributes

An assignment in the class body — outside any method — creates a **class attribute**, shared by every instance. Assignments to `self.something` create **instance attributes**, one per object:

```python
class Account:
    bank_name = "First Python Bank"     # class attribute
    interest_rate = 0.02                # class attribute

    def __init__(self, owner):
        self.owner = owner              # instance attribute

a = Account("Ada")
b = Account("Grace")

print(a.bank_name, Account.bank_name)   # both work

Account.interest_rate = 0.03
print(a.interest_rate, b.interest_rate) # 0.03 0.03 — every instance sees the change
```

*Class attributes live on the class and are visible through every instance.*

Attribute lookup checks the **instance first**, then the class. So assigning through an instance doesn't change the class attribute — it creates a new instance attribute that hides it:

```python
a.interest_rate = 0.05
print(a.interest_rate)          # 0.05 — a's own attribute
print(b.interest_rate)          # 0.03 — still the class attribute
```

*Assigning through `a` adds an attribute to `a` alone.*

That makes a **mutable** class attribute a trap, for the same reason as a mutable default argument — one object, shared by everyone:

```python
class Team:
    members = []                 # one list, shared by every Team

    def add(self, name):
        self.members.append(name)

t1 = Team()
t2 = Team()
t1.add("Ada")
print(t2.members)                # ['Ada']
```

*`append` mutates the shared class-level list rather than creating a per-instance one.*

The fix is to create the list in `__init__` with `self.members = []`, so each instance gets its own.

**Contrast:** class attributes resemble Java `static` fields, but with a twist: in Java, `a.rate = 0.05` through an instance still sets the one static field for everyone. In Python it quietly creates a per-instance attribute instead.

---

## `@staticmethod` and `@classmethod`

The `@` lines below are **decorators** — they modify the function defined beneath them. We'll use built-in decorators throughout this unit; writing our own is beyond this course.

```python
class Temperature:
    def __init__(self, celsius):
        self.celsius = celsius

    @classmethod
    def from_fahrenheit(cls, fahrenheit):
        return cls((fahrenheit - 32) * 5 / 9)

    @staticmethod
    def is_valid(celsius):
        return celsius >= -273.15

boiling = Temperature.from_fahrenheit(212)
print(boiling.celsius)                  # 100.0
print(Temperature.is_valid(-300))       # False
```

*A class method acting as an alternative constructor, and a static method that needs no instance.*

- A **class method** receives the class itself as its first parameter, named **`cls`** by convention. Its main use is **alternative constructors**. Python can't overload `__init__`, so named class methods like `from_fahrenheit` or `from_json` fill that gap. Because it calls `cls(...)` rather than `Temperature(...)`, it also builds the right type when called on a subclass.
- A **static method** receives neither the instance nor the class. It's just a function that lives in the class's namespace.

**Contrast:** Java puts utility functions in classes because it has nowhere else to put them. Python functions can live directly in a module, so a static method is only worth it when the function is tightly tied to the class. Often a plain module-level function is the more idiomatic choice.

---

## Inheritance and `super()`

A subclass names its parent in parentheses:

```python
class Account:
    def __init__(self, owner, balance=0):
        self.owner = owner
        self.balance = balance

    def describe(self):
        return f"{self.owner}: ${self.balance}"

class SavingsAccount(Account):
    def __init__(self, owner, balance=0, rate=0.02):
        super().__init__(owner, balance)
        self.rate = rate

    def describe(self):
        return super().describe() + f" at {self.rate:.0%}"

savings = SavingsAccount("Grace", 500)
print(savings.describe())               # Grace: $500 at 2%
print(isinstance(savings, Account))     # True
```

*A subclass that extends the parent's initializer and overrides one of its methods, calling the parent's version through `super()`.*

- **Overriding** is simply defining a method with the same name. There's no `@Override` annotation and no `final` to prevent it.
- **`super()`** returns an object that forwards calls to the parent class's implementation.
- Every class ultimately inherits from the built-in **`object`**, which is where default behaviors like printing and equality come from.

The most important difference from Java: **`super().__init__()` is never called for us.** If a subclass defines `__init__` and forgets to call the parent's, the parent's attributes are never created, and the failure only shows up later as an `AttributeError`.

**Contrast:** a Java constructor that doesn't call `super(...)` gets an implicit call to the parent's no-argument constructor inserted by the compiler. Python inserts nothing.

Python also supports multiple inheritance — listing several parent classes — but that's outside the scope of this course.

---

## No Access Modifiers

Python has no `private`, `protected`, or `public`. Everything is accessible; conventions signal intent instead:

```python
class Account:
    def __init__(self, owner, balance):
        self.owner = owner              # public
        self._balance = balance         # internal, by convention
        self.__audit_id = 42            # name-mangled

acct = Account("Ada", 100)

print(acct._balance)                    # 100 — allowed, just impolite
print(acct.__audit_id)                  # AttributeError
print(acct._Account__audit_id)          # 42
```

*A single underscore is a convention; a double underscore triggers name mangling.*

- **A single leading underscore** means "internal — use at your own risk." Nothing enforces it; tools and readers respect it.
- **A double leading underscore** triggers **name mangling**: inside the class, `__audit_id` is rewritten to `_Account__audit_id`. The purpose is to stop a subclass from accidentally overwriting a parent's attribute with the same name. It isn't security, as the last line shows.

The Python community's shorthand for this philosophy is "we're all consenting adults here": the language trusts callers to respect the underscore.

---

## `@property` Instead of Getters and Setters

Java classes wrap fields in getters and setters up front, in case validation is ever needed. Python starts with a plain public attribute — and if validation becomes necessary later, **`@property`** adds it without changing a single caller:

```python
class Account:
    def __init__(self, owner, balance=0):
        self.owner = owner
        self.balance = balance          # runs the setter below

    @property
    def balance(self):
        return self._balance

    @balance.setter
    def balance(self, value):
        if value < 0:
            raise ValueError("balance cannot be negative")
        self._balance = value

acct = Account("Ada", 100)
acct.balance = 250          # looks like attribute assignment; calls the setter
print(acct.balance)         # looks like attribute access; calls the getter
acct.balance = -5           # ValueError: balance cannot be negative
```

*Callers still write `acct.balance`, but reads and writes now pass through methods. `raise` signals an error — it's covered in the Exceptions lesson.*

The actual value is stored under `_balance`, since `balance` is now the property's name.

A property without a setter is **read-only**, which suits values computed from other attributes:

```python
class Rectangle:
    def __init__(self, width, height):
        self.width = width
        self.height = height

    @property
    def area(self):
        return self.width * self.height

print(Rectangle(3, 4).area)     # 12 — no parentheses
```

*A computed, read-only attribute.*

**Contrast:** because a plain attribute can become a property later with no change to calling code, Python never needs defensive `getX()`/`setX()` pairs.

---

## `@dataclass`

For classes that mostly hold data, the **`@dataclass`** decorator generates the boilerplate. Fields are declared as annotated class-level names:

```python
from dataclasses import dataclass, field

@dataclass
class Product:
    name: str
    price: float
    tags: list[str] = field(default_factory=list)

lamp = Product("Lamp", 39.99)
same = Product("Lamp", 39.99)

print(lamp)             # Product(name='Lamp', price=39.99, tags=[])
print(lamp == same)     # True — compared field by field
```

*A dataclass with two required fields and one defaulted field, printed and compared without writing any methods.*

From those annotations, `@dataclass` generates `__init__`, a readable string representation, and value-based equality. A few details matter:

- This is the one place annotations are *used* — the decorator reads them to find the fields. They're still **not enforced**: `Product("Lamp", "cheap")` runs fine.
- A mutable default like `tags: list[str] = []` is rejected with a `ValueError`. `field(default_factory=list)` creates a fresh list per instance — the dataclass version of the mutable-default fix.
- **`@dataclass(frozen=True)`** makes instances immutable: assigning to a field raises an error. Frozen dataclasses can also be dict keys and set members; the next lesson explains why ordinary ones can't.

**Contrast:** `@dataclass` collapses the constructor, `equals`, `hashCode`, and `toString` that a Java `record` or a Lombok `@Data` class exists to generate. A frozen dataclass is the closest match to a record, since records are immutable.

---

## Attributes Can Be Added at Runtime

Since attributes are just assignments, nothing stops code from adding new ones to an instance from outside the class:

```python
acct = Account("Ada", 100)
acct.nickname = "rainy-day fund"    # a new attribute on this one object
print(acct.nickname)

acct.balanse = 500                  # typo — silently creates another attribute
print(acct.balance)                 # still 100
```

*Adding attributes from outside the class, including one created by accident.*

Adding attributes deliberately is rare in well-structured code. Adding one accidentally, through a typo, is the real hazard: there's no error, and the intended attribute is simply never updated.

**Contrast:** Java and TypeScript both reject `acct.balanse = 500` at compile time. Python accepts it, which is one more reason to keep attribute assignment inside the class's own methods.

---

## Key Takeaways

- `class` defines a class; `__init__` initializes new instances, which are created by calling the class — no `new`.
- Every instance method takes `self` explicitly, and attribute access inside methods always goes through it.
- There are no field declarations: an attribute exists once assigned. Assign all of them in `__init__`.
- Class attributes are shared; instance attributes are per object. Assigning through an instance creates an instance attribute, and mutable class attributes are shared by everyone.
- `@classmethod` receives `cls` and is the idiom for alternative constructors; `@staticmethod` receives nothing and is often better as a module-level function.
- Subclasses override by redefining a method and reach the parent through `super()`. `super().__init__()` is never called automatically.
- There are no access modifiers: `_name` signals internal, and `__name` triggers name mangling, not privacy.
- `@property` turns attribute access into method calls, so plain attributes can gain validation later without changing callers.
- `@dataclass` generates `__init__`, representation, and equality from annotated fields; use `field(default_factory=...)` for mutable defaults and `frozen=True` for immutability.
- Attributes can be added to any instance at runtime, so typos create new attributes instead of errors.
