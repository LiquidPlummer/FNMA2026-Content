import traceback

def parse_integer(text: str) -> int:
    integer_value = 0
    try:
        integer_value =  int(text)
    except (ValueError, TypeError) as error:
        print("ValueError: That is not a valid number.")
        traceback.print_exc()
        return integer_value
    except Exception as error:
        print("Exception: That is not a valid number.")
        return integer_value
    else:
        print("Else!")
    finally: 
        print("Finally!")
    return integer_value


print(parse_integer("six"))



print("This is the end of the program")





def raise_exception(exception=ValueError):
    """
    Function for raising a ValueError for testing purposes.

    Raises: ValueError
    """
    raise exception("This was a test of the emergency raise exception system")


try:
    raise_exception()
except ValueError as error:
    raise Exception("We re-raised and changed the exception type.") from error



