def shout(text: str):
    return text.upper() + "!"

speak = shout
print(speak("hello"))

def apply_twice(func, value):
    return func(func(value))


print(apply_twice(shout, "hi"))


