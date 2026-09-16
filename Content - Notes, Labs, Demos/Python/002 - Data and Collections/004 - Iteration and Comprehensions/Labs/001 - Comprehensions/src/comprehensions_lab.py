# Comprehensions Lab
#
# ORDERS is one morning of orders from a coffee cart. Below it, every numbered
# print() line has its expression blanked out with None.
#
# Replace each None with a comprehension, run the file, and compare the output
# with the README. Keep going until every line matches:
#
#     python src/comprehensions_lab.py

ORDERS = [
    {"id": 1001, "customer": "ada", "product": "espresso", "qty": 2, "price": 3.25, "region": "north"},
    {"id": 1002, "customer": "grace", "product": "latte", "qty": 1, "price": 4.50, "region": "south"},
    {"id": 1003, "customer": "ada", "product": "muffin", "qty": 3, "price": 2.75, "region": "north"},
    {"id": 1004, "customer": "linus", "product": "espresso", "qty": 1, "price": 3.25, "region": "east"},
    {"id": 1005, "customer": "margaret", "product": "latte", "qty": 2, "price": 4.50, "region": "south"},
    {"id": 1006, "customer": "grace", "product": "tea", "qty": 4, "price": 2.00, "region": "south"},
    {"id": 1007, "customer": "alan", "product": "espresso", "qty": 3, "price": 3.25, "region": "north"},
]

# Extracting a field
print("1. every order's customer:", None)

# The same thing with {}
print("2. customers, each once:", None)

# Adding an if filter
print("3. ids of orders with a quantity of 3 or more:", None)

# f-strings as the output
print("4. order labels:", None)

# Dict comprehensions
print("5. customer by order id:", None)

# sorted() for stable output
print("6. customers, each once, alphabetically:", None)

# On your own
print("7. every order's product:", None)
print("8. quantities ordered, each once:", None)
print("9. customers of orders in the south:", None)
print("10. line totals for the north:", None)
print("11. price by product:", None)
print("12. regions, each once, alphabetically:", None)
