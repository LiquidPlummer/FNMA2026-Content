# Classes and Dunders Lab — the driver
#
# Block 0 runs as-is. Every other block is commented out.
#
# Work through the blocks in order:
#   1. Uncomment the next block — every line from its ## header down to the
#      blank line after it. (In VS Code, select the lines and press Ctrl+/,
#      or Cmd+/ on macOS. The ## header stays a comment.)
#   2. Run the file and see what breaks, or what looks wrong.
#   3. Change basket.py until the block's output matches the README.
#
# Run from the lab folder (the one containing README.md):
#
#     python src/main.py

from basket import Basket, Item

bread = Item("bread", 350)
milk = Item("milk", 199)
coffee = Item("coffee", 1299)

basket = Basket("ada")
basket.add(bread)
basket.add(milk)
basket.add(coffee)

## Block 0: starting point
print("== 0. Starting point ==")
print(basket.owner)
print(basket.get_total())

## Block 1: @staticmethod
# print("== 1. @staticmethod ==")
# print(Basket.format_cents(1848))
# print(basket.format_cents(99))

## Block 2: __str__ and __repr__
# print("== 2. __str__ and __repr__ ==")
# print(basket)
# print(repr(basket))
# print([basket])

## Block 3: __len__
# print("== 3. __len__ ==")
# print(len(basket))
# empty = Basket("nobody")
# print("empty is falsy:", not empty)

## Block 4: __contains__
# print("== 4. __contains__ ==")
# print("bread" in basket)
# print("cheese" in basket)

## Block 5: __iter__
# print("== 5. __iter__ ==")
# for item in basket:
#     print(item.name, Basket.format_cents(item.price))

## Block 6: @property
# print("== 6. @property ==")
# print(Basket.format_cents(basket.total))

## Block 7: __add__
# print("== 7. __add__ ==")
# other = Basket("grace")
# other.add(Item("apples", 425))
# other.add(Item("jam", 575))
# combined = basket + other
# print(combined)
# print(len(basket), len(other), len(combined))

## Block 8: __eq__ and __hash__
# print("== 8. __eq__ and __hash__ ==")
# print(Item("bread", 350) == Item("bread", 350))
# print(Item("bread", 350) == Item("bread", 399))
# print(len({Item("bread", 350), Item("bread", 350), Item("milk", 199)}))

## Block 9: @dataclass
# print("== 9. @dataclass ==")
# print(bread)
# print([milk, coffee])

## Block 10: class and instance attributes
# print("== 10. Class and instance attributes ==")
# print("baskets created:", Basket.baskets_created)
# print(basket.currency, other.currency)
# other.currency = "EUR"
# print(basket.currency, other.currency, Basket.currency)
## Explain: why did assigning other.currency leave basket.currency and
## Basket.currency alone?

## Block 11: __enter__ and __exit__
# print("== 11. __enter__ and __exit__ ==")
# with Basket("linus") as order:
#     order.add(Item("tea", 650))
#     order.add(milk)
#     print("items in order:", len(order))
# print("items after checkout:", len(order))

## Exercise 1
# print("== Exercise 1 ==")
# print(basket[0])
# print(basket[-1].name)

## Exercise 2
# print("== Exercise 2 ==")
# del basket[0]
# print(len(basket), "bread" in basket)
