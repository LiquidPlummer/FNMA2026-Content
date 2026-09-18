def name_of_function() -> None:
    # As long as we stay indented, we're still in the block of code
    print("This is inside the function")

print("This is outside the function") # Here the indent drops back to 0




result: None = name_of_function()
print(result)
print(type(result))