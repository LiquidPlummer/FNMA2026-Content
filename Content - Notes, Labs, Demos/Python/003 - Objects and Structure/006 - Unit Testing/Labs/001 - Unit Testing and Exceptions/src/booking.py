"""Meeting-room bookings for a small office."""

ROOMS = {"atlas": 8, "boreal": 4, "cirrus": 12}
HOURLY_RATE = 25


class BookingError(Exception):
    """Raised when a booking request can't be honored."""


def parse_time(text):
    """Return the number of minutes after midnight for a time written "HH:MM".

    parse_time("09:30") returns 570. Raise BookingError if text isn't a valid
    time of day.
    """
    try:
        hours, minutes = text.split(":")
        hours = int(hours)
        minutes = int(minutes)
    except ValueError as error:
        raise BookingError(f"invalid time: {text}") from error
    if not (0 <= hours < 24 and 0 <= minutes < 60):
        raise BookingError(f"invalid time: {text}")
    return hours * 60 + minutes


def capacity_of(room):
    """Return how many people room holds. Raise BookingError if there's no such room."""
    if room not in ROOMS:
        raise BookingError(f"unknown room: {room}")
    return ROOMS[room]


def overlaps(start, end, other_start, other_end):
    """Return True if the time range start-end overlaps other_start-other_end.

    Ranges that only touch, with one ending exactly when the other begins,
    don't overlap.
    """
    return start <= other_end and other_start <= end


def book(bookings, room, people, start_text, end_text):
    """Add a booking to the bookings list and return the new booking's id.

    Each booking is a dict with "id", "room", "people", "start", and "end" keys,
    with start and end in minutes after midnight. Ids count up from 1.

    Raise BookingError if the room doesn't exist, the group is bigger than the
    room holds, the booking doesn't end after it starts, or the room is already
    booked for any part of that time.
    """
    capacity = capacity_of(room)
    if people >= capacity:
        raise BookingError(f"{room} holds {capacity} people, not {people}")
    start = parse_time(start_text)
    end = parse_time(end_text)
    if end <= start:
        raise BookingError("a booking must end after it starts")
    for existing in bookings:
        if existing["room"] == room and overlaps(start, end, existing["start"], existing["end"]):
            raise BookingError(f"{room} is already booked at that time")
    booking = {"id": len(bookings) + 1, "room": room, "people": people, "start": start, "end": end}
    bookings.append(booking)
    return booking["id"]


def price(booking):
    """Return what a booking costs.

    Every hour that's started is billed in full at HOURLY_RATE, so a 90-minute
    booking is billed as 2 hours.
    """
    minutes = booking["end"] - booking["start"]
    return minutes // 60 * HOURLY_RATE


def process_requests(bookings, requests):
    """Try to book each request in order, and report what happened.

    Each request is a dict with "room", "people", "start", and "end" keys.
    Return a dict with three keys: "booked", a list of the new booking ids;
    "rejected", a list of the error messages from requests that failed; and
    "processed", a count of every request handled, whether it was booked or not.
    """
    report = {"booked": [], "rejected": [], "processed": 0}
    for request in requests:
        try:
            booking_id = book(bookings, request["room"], request["people"], request["start"], request["end"])
        except BookingError as error:
            report["rejected"].append(str(error))
        else:
            report["booked"].append(booking_id)
            report["processed"] += 1
    return report
