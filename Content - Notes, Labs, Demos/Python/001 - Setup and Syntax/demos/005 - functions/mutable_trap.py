
def add_item(item, cart=[]):
    cart.append(item)
    return cart


print(add_item("apple"))
print(add_item("bread"))