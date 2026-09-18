class Rect:
    def __init__(self, dimensions, name="a rectangle"):
        self.w, self.h = dimensions
        self._name = name

    @property
    def name(self):
        return self._name

    @name.setter
    def name(self, name):
        self._name = name
    
    @property
    def area(self):
        """
        This one is essentially a getter, and we can treat it like it's a property
        """
        return self.w * self.h

    @area.setter
    def area(self, dimensions):
        """
        This one is essentially a setter, we can pass it a value like asigning a property
        """
        self.w, self.h = dimensions

    def __str__(self):
        return f"{self.name}: {self.w} x {self.h} = area of {self.area} units squared."


rect = Rect((2,3))
print(rect.area)
rect.area = (5,5)
print(rect.area)
print(rect)