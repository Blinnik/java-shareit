package ru.practicum.shareit.item.model.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.model.dto.BookingBookerIdDto;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Data
@Builder
@AllArgsConstructor
public class ItemBookingsAndCommentsDto {
    Long id;

    String name;

    String description;

    Boolean available;

    BookingBookerIdDto lastBooking;

    BookingBookerIdDto nextBooking;

    List<CommentDto> comments;
}