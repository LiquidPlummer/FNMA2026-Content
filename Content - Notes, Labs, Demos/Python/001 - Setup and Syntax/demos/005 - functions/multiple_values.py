def min_max(numbers):
    return min(numbers), max(numbers), -1

# print(min_max([4, 9, 1, 7]))

_, high, _ = min_max([4, 9, 1, 7])
print(high)
