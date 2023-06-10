package ru.practicum.shareit.booking.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.Instant;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
public class Booking {
    Long id;
    Instant start;
    Instant end;
    Item item;
    User booker;
    BookingStatus status;
}
