def convert(amount: float, rate: float) -> float:
    """Convert an amount into another currency.

    Multiplies amount by the exchange rate and rounds to two decimal places.

     - amount: float to be converted
     - rate: float used as factor for conversion
     - returns: float
    """
    return round(amount * rate, 2)

print(convert(100, 0.92))
print(convert.__doc__)