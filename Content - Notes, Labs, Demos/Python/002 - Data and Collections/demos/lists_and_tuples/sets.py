# What do we know about sets?
# All elements are unique - no dupes
# no order

my_set = set(["a", "b", "a"])
print(my_set)


my_set = my_set.union(["d", "e"])
print(my_set)

my_set.discard("a")
my_set.discard("x")
# my_set.remove("x")
print(my_set)


a = {1, 2, 3}
b = {3, 4, 5}

print("union: ", a | b)
print("intersection: ", a & b)
print("a-b diff: ", a - b)
print("b-a diff: ", b - a)
print("symmetric diff: ", a ^ b)
print("test: ", (a-b) | (b-a))

