# Writing Functions Lab
#
# The top half of this file holds numbered function stubs, each with a
# docstring stating its contract. The bottom half is a driver block that
# calls the stubs and prints the results.
#
# Work through the numbers in order: implement the stub, finish any TODOs in
# that number's part of the driver, then run the file and compare with the
# expected output in the README:
#
#     python src/functions_lab.py


# 1. Returning a value
def format_price(amount):
    """Return amount as a price string with a dollar sign and two decimals.

    format_price(3.5) returns "$3.50".
    """
    pass  # TODO: implement


# 2. Returning nothing
def announce(message):
    """Print message with three asterisks and a space on each side.

    announce("Hi") prints "*** Hi ***". Returns nothing.
    """
    pass  # TODO: implement


# 3. Returning two values
def price_range(prices):
    """Return the lowest and the highest price in prices, in that order."""
    pass  # TODO: implement


# 4. Default values
def make_order(drink, size, shots):
    """Return a one-line description of a coffee order.

    The format is "<size> <drink>, <shots> shot(s)", for example
    "medium latte, 1 shot(s)". size defaults to "medium" and shots
    defaults to 1.
    """
    pass  # TODO: give size and shots their default values, then implement


# 5. The mutable default trap
def add_topping_buggy(topping, toppings=[]):
    """Append topping to toppings and return toppings.

    If no toppings list is passed in, start from an empty list.
    """
    pass  # TODO: implement exactly as described, leaving the signature alone


def add_topping_fixed(topping, toppings=[]):
    """Append topping to toppings and return toppings.

    If no toppings list is passed in, start from a new empty list on
    every call.
    """
    pass  # TODO: fix the signature, then implement


# 6. Any number of positional arguments
def order_total():
    """Return the sum of any number of prices passed as separate arguments.

    order_total(3.5, 2.25) returns 5.75, and order_total() returns 0.
    """
    pass  # TODO: add a parameter that collects the prices, then implement


# 7. Any number of keyword arguments
def print_receipt(customer):
    """Print "Receipt for <customer>", then one line per extra keyword argument.

    Each extra line is two spaces, the argument's name, a colon, a space,
    and its value: print_receipt("Ada", latte=4.25) prints "Receipt for Ada"
    and then "  latte: 4.25". Returns nothing.
    """
    pass  # TODO: add a parameter that collects keyword arguments, then implement


# 8. Functions as objects
def shout(text):
    """Return text in uppercase, followed by an exclamation mark."""
    pass  # TODO: implement


def apply_to_each(transform, words):
    """Call transform on each word in words, printing each result on its own line."""
    pass  # TODO: implement


# 9. lambda as a sort key
def sort_by_price(menu):
    """Return a new list of (name, price) pairs sorted from cheapest to priciest."""
    pass  # TODO: implement using sorted() with a lambda as the key


# 10. Annotations
def scale_recipe(amount, servings):
    """Return amount multiplied by servings.

    Meant for numbers: scale_recipe(2.5, 4) returns 10.0.
    """
    pass  # TODO: annotate amount as float, servings as int, and the return as float; then implement


if __name__ == "__main__":
    print("== 1. Returning a value ==")
    print(format_price(3.5))
    print(format_price(12))

    print("== 2. Returning nothing ==")
    result = announce("Now serving order 42")
    print("announce returned:", result)

    print("== 3. Returning two values ==")
    menu_prices = [4.25, 2.75, 5.5, 3.0]
    # TODO: replace these two lines with ONE line that unpacks
    # price_range(menu_prices) into lowest and highest.
    lowest = None
    highest = None
    print("cheapest:", lowest)
    print("priciest:", highest)

    print("== 4. Default values ==")
    # TODO: replace each None with a call to make_order:
    #   - "mocha", using both defaults
    #   - "latte", passing shots=2 by keyword and leaving size at its default
    #   - "americano", passing shots=3 and then size="large", both by keyword
    print(None)
    print(None)
    print(None)

    print("== 5. The mutable default trap ==")
    print("buggy, order 1:", add_topping_buggy("vanilla"))
    print("buggy, order 2:", add_topping_buggy("caramel"))
    print("fixed, order 1:", add_topping_fixed("vanilla"))
    print("fixed, order 2:", add_topping_fixed("caramel"))
    # Explain: why does the buggy second order still contain vanilla?

    print("== 6. Any number of positional arguments ==")
    # TODO: replace the first None with order_total(3.5, 2.25, 4.0), and the
    # second with order_total() called with no arguments.
    print("three items:", None)
    print("no items:", None)

    print("== 7. Any number of keyword arguments ==")
    # TODO: call print_receipt for "Ada", with latte=4.25 and muffin=3.0.

    print("== 8. Functions as objects ==")
    # TODO: bind the name loud to the shout function itself (no parentheses).
    loud = None
    # TODO: replace None with loud("order up").
    print(None)
    # TODO: call apply_to_each, passing shout itself and ["latte", "mocha"].

    print("== 9. lambda as a sort key ==")
    menu = [("latte", 4.25), ("espresso", 2.75), ("mocha", 5.5)]
    print(sort_by_price(menu))

    print("== 10. Annotations ==")
    print(scale_recipe(2.5, 4))
    print(scale_recipe("mocha", 2))
    print(scale_recipe.__annotations__)
