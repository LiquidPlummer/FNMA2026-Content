# Predict the Output
#
# Do NOT run this file yet. For each question, read the code and write what
# you expect it to print on its "Prediction:" line. Once every prediction is
# written down, run the file and check:
#
#     python src/predict.py
#
# For any prediction that turned out wrong, add a comment explaining why the
# real output is what it is.


# Q1
# Prediction:
print("Q1:", 10 / 5)


# Q2
# Prediction:
print("Q2:", -9 // 4, -9 % 4)


# Q3
# Prediction:
print("Q3:", [] or [0] or "fallback")


# Q4
# Prediction:
print("Q4:", 1 < 3 > 2)


# Q5
# Prediction:
print("Q5:", "5" == 5)


# Q6
# Prediction:
count = 3
++count
print("Q6:", count)


# Q7
# Prediction:
print("Q7:", 0 or "" or None)


# Q8
# Prediction:
total = 0
for n in range(1, 5):
    if n == 3:
        continue
    total += n
print("Q8:", total)


# Q9
# Prediction:
countdown = 3
while countdown > 0:
    countdown -= 1
print("Q9:", countdown)


# Q10
# Prediction:
for n in [2, 4, 6]:
    if n % 2 == 1:
        print("Q10: odd number found")
        break
else:
    print("Q10: no odd numbers")
