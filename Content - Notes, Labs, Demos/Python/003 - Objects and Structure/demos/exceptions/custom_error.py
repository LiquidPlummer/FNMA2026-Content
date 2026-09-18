class CustomError(Exception):
    """This is our custom exception class..."""


class CustomBetterError(CustomError):
    """This one will have a more robust implentation..."""
    def __init__(self, message):
        super().__init__(message)
        # maybe here we automatically log to our log sink