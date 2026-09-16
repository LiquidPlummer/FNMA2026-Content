# Classes and Dunders Lab — the classes
#
# Item and Basket start out as plain classes. Each driver block in main.py
# needs something new from them: a dunder method, a decorator, or a change of
# approach. We add those here, one block at a time.


class Item:
    """One product: a name and a price in cents."""

    def __init__(self, name, price):
        self.name = name
        self.price = price


class Basket:
    """A shopping basket belonging to one owner."""

    currency = "USD"
    baskets_created = 0

    def __init__(self, owner):
        self.owner = owner
        self._items = []
        Basket.baskets_created += 1

    def add(self, item):
        """Add item to the end of the basket."""
        self._items.append(item)

    def get_total(self):
        """Return the total price of every item in the basket, in cents."""
        return sum(item.price for item in self._items)
