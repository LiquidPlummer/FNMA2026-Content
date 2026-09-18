import pytest
from sut import add, divide
# What are the three A's of unit testing?



@pytest.fixture # This is part of "arrange", we're doing it up here and using it for multiple tests
def math():
    return {"a": 10, "b": 5, "sum": 15, "quotent": 2, "product":50, "difference":5}

# Arrange
# Act
# Assert
def test_add():
    # Arrange
    a = 8
    b = 9
    expected = 17

    # Act
    result = add(a, b)
    result_2 = add(b, a)

    # Assert
    assert(expected==result and expected == result_2)

# We are shadowing the previous test in favor of this one which takes in our fixture
# def test_add(math):
#     # Arrange
#     # a = 8
#     # b = 9
#     # expected = 17

#     # Act
#     result = add(math.a, math.b)
#     result_2 = add(math.b, math.a)

#     # Assert
#     assert(math.sum == result and math.sum == result_2)
        


def test_divide(math):
    with pytest.raises(ZeroDivisionError):
        expected = 2
        result = divide(10, 5)
        assert(result==expected)
        


# test_add(math)
test_divide(math)

