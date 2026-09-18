class MyClass:
    def my_func(self):
        print("test")

    def __init__(self, variable):
        self.variable = variable

    def __str__(self):
        return f"Variable is: {self.variable}"

    def __repr__(self):
        """
        This is __str__ for the REPL
        """

    def __len__(self):
        return len(self.variable)

    def __eq__(self, other):
        return (type(self) == type(other)) and (self.variable == other.variable)

    def __contains__(self, match_value):
        return match_value in self.variable

    def __iter__(self):
        return self.variable.__iter__()

    def __add__():
        """
        We can define what it would mean to use the + operator on this class like my_class + my_class
        """

my_class = MyClass("value")
my_other_class = MyClass("value")
my_class.my_func()
print(str(my_class))
print(len(my_class))
print(my_class == my_other_class)
print("z" in my_class)
for c in my_class:
    print(c)