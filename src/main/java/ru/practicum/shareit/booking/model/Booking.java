package ru.practicum.shareit.booking.model;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.Instant;

public class Booking {
    Long id;
    Instant start;
    Instant end;
    Item item;
    User booker;
    BookingStatus status;
}
