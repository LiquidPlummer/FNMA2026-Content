# ARGS and KWARGS
# ARGS are arguments, passed to functions. Positional Arguments passed by index
# KWARGS key word ARGS, passed to functions. KWARGS passed by name - key/value pairs


def create_account(owner, balance, currency):
    return f"{owner}: {balance} {currency}"


print(create_account("Kyle", currency="USD", balance=1000))

# This was a test to see if the kwargs object acts like a set, which removes dupes. 
# Turns out it does not. The dupes are maintained, it's a "set-like dict"
def test_dupe_kwargs(**kwargs):
    sum = 0
    for key, value in kwargs.items():
        print("kwarg name: ", key)
        sum += value
    return sum

print(test_dupe_kwargs(a=1,b=1,c=1))
    

def add(a: int, b: int, *args):
    sum = a + b
    for val in args:
        sum += val
    return sum

print(add(1,2,3,4,5,6,7))


def add(a=0, b=0, *args, **kwargs):
    sum = a + b
    print("new sum: ", sum)
    for value in args:
        sum += value
        print("new sum: ", sum)

    # for value in kwargs.values():
    for _, value in kwargs.items():
        sum += value
        print("new sum: ", sum)
    return sum



print(add(1,2,3,4,5,6,7,8, c=9, d=10, e=11))