
def create_account(owner, balance=0, currency="USD"):
    return f"{owner}: {balance} {currency}"


print(create_account(owner="Kyle", currency="YEN"))




# No overloading in python, instead the second def "shadows" the first
def add(a: int, b: int):
    return a+b

def add(a: float, b: float):
    return a-b

print(add(1.0, 99.0))

