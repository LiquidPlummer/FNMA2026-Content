

# integer - no min or max, arbitrary in length
big_value = 2 ** 100
print("Type of big_value: " + str(type(big_value)))
print("big_value: " + str(big_value))



# Floats act like Java "Double", based on the same standard
float_value = 0.1 + 0.2
print("Type of value: " + str(type(float_value)))
print("float_value: " + str(float_value))

bool_value = True
print("Type of bool_value: " + str(type(bool_value)))
print("bool_value: " + str(bool_value))
print("isinstance: " + str(isinstance(0.1, (int, str))))



