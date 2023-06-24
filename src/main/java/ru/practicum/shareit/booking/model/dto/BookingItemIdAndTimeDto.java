package ru.practicum.shareit.booking.model.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotNull;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Data
public class BookingItemIdAndTimeDto {
    @NotNull
    Long itemId;

    @NotNull
    String start;

    @NotNull
    String end;
}
