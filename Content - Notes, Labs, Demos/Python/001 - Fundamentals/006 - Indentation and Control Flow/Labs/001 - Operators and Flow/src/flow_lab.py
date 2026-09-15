# Operators and Flow Lab
#
# Work through the numbered sections from top to bottom. Each section lists
# the exact output it should produce once its TODOs are done. Run the file
# after finishing each section and compare:
#
#     python src/flow_lab.py
#
# Some sections end with an "Explain:" prompt. Answer it with a comment of a
# sentence or two, directly under the prompt.


# ==========================================================================
# 1. Division
# Expected output:
#   == 1. Division ==
#   7 / 2 = 3.5
#   7 // 2 = 3
#   -7 // 2 = -4
#   -7 % 2 = 1
# ==========================================================================
print("== 1. Division ==")

# TODO: replace each None with the expression written in its label.
print("7 / 2 =", None)
print("7 // 2 =", None)
print("-7 // 2 =", None)
print("-7 % 2 =", None)

# Explain: Java gives -3 for -7 / 2. Why does Python give -4 for -7 // 2?


# ==========================================================================
# 2. Exponents
# Expected output:
#   == 2. Exponents ==
#   2 ** 10 = 1024
#   -2 ** 2 = -4
#   (-2) ** 2 = 4
# ==========================================================================
print("== 2. Exponents ==")

# TODO: replace each None with the expression written in its label.
print("2 ** 10 =", None)
print("-2 ** 2 =", None)
print("(-2) ** 2 =", None)

# Explain: why is -2 ** 2 negative?


# ==========================================================================
# 3. Augmented assignment
# Expected output:
#   == 3. Augmented assignment ==
#   after adding 5: 15
#   after subtracting 3: 12
#   after doubling: 24
# ==========================================================================
print("== 3. Augmented assignment ==")
stock = 10

# TODO: add 5 to stock using augmented assignment.
print("after adding 5:", stock)

# TODO: subtract 3 from stock using augmented assignment.
print("after subtracting 3:", stock)

# TODO: double stock using augmented assignment.
print("after doubling:", stock)

# Explain: Python has no ++ operator. In the REPL, run stock = 10 and then
# ++stock. What does ++stock do, and why doesn't it change stock?


# ==========================================================================
# 4. == vs is
# Expected output:
#   == 4. == vs is ==
#   first_order == second_order: True
#   first_order is second_order: False
#   first_order is same_order: True
# ==========================================================================
print("== 4. == vs is ==")
first_order = [3, 1, 2]
second_order = [3, 1, 2]
same_order = first_order

# TODO: replace each None with the comparison written in its label.
print("first_order == second_order:", None)
print("first_order is second_order:", None)
print("first_order is same_order:", None)

# Explain: first_order and second_order hold the same numbers. Why is one
# comparison between them True and the other False?


# ==========================================================================
# 5. Chained comparisons
# Expected output:
#   == 5. Chained comparisons ==
#   72 is comfortable: True
#   90 is comfortable: False
# ==========================================================================
print("== 5. Chained comparisons ==")
temperature = 72

# TODO: replace None with ONE chained comparison that checks whether
# temperature is between 68 and 76, inclusive.
print("72 is comfortable:", None)

temperature = 90

# TODO: the same chained comparison again.
print("90 is comfortable:", None)


# ==========================================================================
# 6. and / or
# Expected output:
#   == 6. and / or ==
#   display name: ada_l
#   0 and 99 -> 0
#   5 and 99 -> 99
#   [] or 'empty' -> empty
# ==========================================================================
print("== 6. and / or ==")
nickname = ""
username = "ada_l"

# TODO: replace None with: nickname or username
print("display name:", None)

# TODO: replace each None with the expression written in its label.
print("0 and 99 ->", None)
print("5 and 99 ->", None)
print("[] or 'empty' ->", None)

# Explain: in Java, && and || always produce true or false. What do and / or
# produce in Python?


# ==========================================================================
# 7. Truthiness
# Expected output:
#   == 7. Truthiness ==
#   bool(0): False
#   bool(''): False
#   bool([]): False
#   bool(None): False
#   bool('0'): True
#   bool([0]): True
# ==========================================================================
print("== 7. Truthiness ==")

# TODO: replace each None with the bool(...) call written in its label.
print("bool(0):", None)
print("bool(''):", None)
print("bool([]):", None)
print("bool(None):", None)
print("bool('0'):", None)
print("bool([0]):", None)

# Explain: '0' and [0] look like "zero" or "empty". Why are they truthy?


# ==========================================================================
# 8. Checking for empty
# Expected output:
#   == 8. Checking for empty ==
#   cart is empty
#   cart has items
# ==========================================================================
print("== 8. Checking for empty ==")
cart = []

# TODO: using `if not cart:`, print "cart is empty" when cart is empty,
# and "cart has items" otherwise.

cart = ["coffee"]

# TODO: the same check again.


# ==========================================================================
# 9. Membership
# Expected output:
#   == 9. Membership ==
#   'M' in sizes: True
#   'XL' in sizes: False
#   'XL' not in sizes: True
#   'tea' in 'steam': True
# ==========================================================================
print("== 9. Membership ==")
sizes = ["S", "M", "L"]

# TODO: replace each None with the expression written in its label.
print("'M' in sizes:", None)
print("'XL' in sizes:", None)
print("'XL' not in sizes:", None)
print("'tea' in 'steam':", None)


# ==========================================================================
# 10. Conditional expressions
# Expected output:
#   == 10. Conditional expressions ==
#   1 item
#   4 items
# ==========================================================================
print("== 10. Conditional expressions ==")
quantity = 1

# TODO: replace None with ONE conditional expression that gives "item" when
# quantity is 1 and "items" otherwise.
label = None
print(quantity, label)

quantity = 4

# TODO: the same conditional expression again.
label = None
print(quantity, label)


# ==========================================================================
# 11. if / elif / else
# Expected output:
#   == 11. if / elif / else ==
#   grade: B
# ==========================================================================
print("== 11. if / elif / else ==")
score = 83

# TODO: replace the line below with an if / elif / else chain that sets grade
# to "A" for 90 and up, "B" for 80 and up, "C" for 70 and up, and "F" otherwise.
grade = None
print("grade:", grade)


# ==========================================================================
# 12. while
# Expected output:
#   == 12. while ==
#   3
#   2
#   1
#   liftoff
# ==========================================================================
print("== 12. while ==")
countdown = 3

# TODO: write a while loop that prints countdown and then lowers it by 1,
# stopping once countdown reaches 0.

print("liftoff")


# ==========================================================================
# 13. for loops
# Expected output:
#   == 13. for loops ==
#   Ada
#   Grace
#   Linus
#   Ada
#   Grace
#   Linus
# ==========================================================================
print("== 13. for loops ==")
crew = ["Ada", "Grace", "Linus"]

# TODO: loop directly over crew and print each name.

# TODO: print the same names again, this time looping over range(len(crew))
# and indexing into crew.

# Explain: both loops print the same names. Which one would a Python code
# reviewer ask us to change, and why?


# ==========================================================================
# 14. break and continue
# Expected output:
#   == 14. break and continue ==
#   reading: 12
#   reading: 7
# ==========================================================================
print("== 14. break and continue ==")
readings = [12, 7, -1, 30, 8]

# TODO: loop over readings and print "reading:" followed by each value, except:
#   - skip negative readings with continue
#   - stop the loop entirely at the first reading above 25 with break


# ==========================================================================
# 15. Loop else
# Expected output:
#   == 15. Loop else ==
#   over limit: 30
#   all readings within limit
# ==========================================================================
print("== 15. Loop else ==")
limit = 25
first_batch = [12, 7, 30, 8]
second_batch = [12, 7, 8]

# TODO: loop over first_batch. At the first reading above limit, print
# "over limit:" followed by the reading, then break. Give the loop an else
# clause that prints "all readings within limit".

# TODO: the same loop again, over second_batch.

# Explain: why did the else clause run for one batch but not the other?


# ==========================================================================
# 16. pass
# Expected output:
#   == 16. pass ==
#   kept: 5
#   kept: 9
# ==========================================================================
print("== 16. pass ==")

# TODO: uncomment the loop below and run the file. It fails before anything
# prints at all. Fix it with pass so negative readings are silently skipped.
#
# for reading in [5, -2, 9]:
#     if reading < 0:
#         # negative readings are ignored for now
#     else:
#         print("kept:", reading)
