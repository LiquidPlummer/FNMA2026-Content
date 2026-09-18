languages = ["Java", "JS", "TS", "Python", "SQL"]
index_zero = languages[0]
index_negative = languages[-1]
sliced = languages[1:3]



index_1 = 0
index_2 = 2
index_3 = 4

first_slice = languages[index_1:index_2]
second_slice = languages[index_2:index_3]

print(first_slice)
print(second_slice)

languages_2 = first_slice
languages_2.append("SQL")
languages_2.extend(second_slice)

print(languages_2)

pop_back = languages_2.pop()
languages_2.remove("JS")
print(pop_back)
print(languages_2)


zero, _, _ = languages_2
print(zero)


languages_2.extend(("Scheme", "Lisp", "Shakespeare"))
print(languages_2)