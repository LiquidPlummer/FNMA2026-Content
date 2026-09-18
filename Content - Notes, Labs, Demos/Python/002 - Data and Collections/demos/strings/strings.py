double_quote_string_literal = "This has double quotes \"To be or not to be\""
single_quoted_string_literal = 'This is encased in single quotes, so we can use "double quotes" in the litertal without escaping. can\'t'
multi_line_string_literal = """
Three double-quotes in a row tells
python that this is a string literal
that should also maintain newlines and whitespace.
    this is an indented line

    
    this is several lines down
"""

first_letter = "Python"[0]


print(f"${12.3456789:.2%}")


name, qty, price = "Widget", 7, 3.5
print(f"|{name:<10}|{qty:>5}|{price:>8.2f}|")

greeting = "Hello" + " Kyle"



report = ""
for line in multi_line_string_literal:
    report += line + "\n"       # copies the whole report every pass

report = "\n".join(multi_line_string_literal)       # one pass, one string


word = "test"
test_string = "This is a test string."
if(word in test_string):
    print("The word was found in the test string")