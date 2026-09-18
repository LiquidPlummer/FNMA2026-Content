# Lists like this: [1,2,3]
# Tuples like this: (1,2,3)
# Dicts like this: 
my_dict = {"one": 1, "two": 2, "three": 3}

print(my_dict["one"])

my_dict["one"] = -1
print(my_dict)


del my_dict["one"]
print(my_dict)


print("two" in my_dict)


# one = my_dict["one"]
other_one = my_dict.get("one")
# print(one)
print(other_one)

print("===================")
for k in my_dict:
    print(k, my_dict[k])

for k in my_dict.keys():
    print(k, my_dict[k])

for k, v in my_dict.items():
    print(k, v)

for v in my_dict.values():
    print(v)


# Objects are k/v pairs, but dicts aren't. Dicts are closer an implentation map, not of an object or class
this_wont_work = {["a", "b"]: 1}  

