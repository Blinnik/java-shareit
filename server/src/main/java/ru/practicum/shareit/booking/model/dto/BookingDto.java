package ru.practicum.shareit.booking.model.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.user.model.dto.ItemBookerDto;

import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Data
@Builder
public class BookingDto {
    Long id;

    LocalDateTime start;

    LocalDateTime end;

    ItemDto item;

    ItemBookerDto booker;

    BookingStatus status;
}
