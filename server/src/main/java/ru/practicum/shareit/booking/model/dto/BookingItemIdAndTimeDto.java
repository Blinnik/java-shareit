package ru.practicum.shareit.booking.model.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Data
public class BookingItemIdAndTimeDto {
    Long itemId;

    LocalDateTime start;

    LocalDateTime end;
}
