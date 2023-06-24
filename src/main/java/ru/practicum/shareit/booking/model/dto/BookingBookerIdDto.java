package ru.practicum.shareit.booking.model.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Data
public class BookingBookerIdDto {
    Long id;

    Long bookerId;
}
