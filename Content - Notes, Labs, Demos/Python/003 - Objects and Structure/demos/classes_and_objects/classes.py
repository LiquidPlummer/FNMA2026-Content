class Account:
    def __init__(self, owner, balance=0):
        self.owner = owner
        self.balance = balance

    def deposit(self, amount):
        self.balance += amount
        return self.balance

acct = Account("Ada", 100)      # no `new`
acct.deposit(50)



class Team:
    def __init__(self, members):
        self.members = members


    members = []                # one list, shared by every Team - this one is effectively static
    def add(self, name):
        self.members.append(name)

    @classmethod
    def from_fahrenheit(cls, fahrenheit):
        return cls((fahrenheit - 32) * 5 / 9)

    @staticmethod
    def is_valid(celsius):
        return celsius >= -273.15




t1, t2 = Team([]), Team([])
t1.add("Ada")
print(t1.members)
print(t2.members)          # ['Ada']




class SpecialTeam(Team):
    def __init__(self, members, specialty):
        super().__init__(members)
        self.specialty  = specialty

    def print_out(self):
        print(self.specialty)
        return self.specialty

web_dev_team = SpecialTeam([], "Web Dev")
web_dev_team.add("Kyle")
web_dev_team.print_out()
print(web_dev_team.members)

backend_team = SpecialTeam([], "backend team")
backend_team.add("Jinwoo")
backend_team.print_out()
print(backend_team.members)



class Rectangle:
    def __init__(self, w, h):
        self.w = w
        self.h = h


# TODO: Check into the getter/setter property syntax, I'm missing something here, notes are wrong.
    @property
    def test_value(self):
        return self.test_value

    @test_value.setter
    def test_value(self, val):
        self.test_value = val


    @property
    def area(self):
        return self.w * self.h

    @area.setter
    def area(self, dimensions):
        self.w, self.h = dimensions




rect = Rectangle(3,4)
print(rect.area)

rect.test_value = "hello"
print(rect.test_value)
