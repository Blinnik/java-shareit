package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.dto.BookingBookerIdDto;
import ru.practicum.shareit.booking.model.dto.BookingDto;
import ru.practicum.shareit.booking.model.dto.BookingItemIdAndTimeDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class BookingMapper {
    public static Booking toBooking(Long bookerId, BookingItemIdAndTimeDto bookingItemIdAndTimeDto) {
        Long itemId = bookingItemIdAndTimeDto.getItemId();
        LocalDateTime start = LocalDateTime.parse(bookingItemIdAndTimeDto.getStart());
        LocalDateTime end = LocalDateTime.parse(bookingItemIdAndTimeDto.getEnd());

        return Booking.builder()
                .item(Item.builder().id(itemId).build())
                .booker(User.builder().id(bookerId).build())
                .start(start)
                .end(end)
                .build();
    }

    public static BookingDto toBookingDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(ItemMapper.toItemDto(booking.getItem()))
                .booker(UserMapper.toItemBookerDto(booking.getBooker()))
                .status(booking.getStatus())
                .build();
    }

    public static List<BookingDto> toBookingDto(List<Booking> bookings) {
        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    public static BookingBookerIdDto toBookingBookerIdDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        return new BookingBookerIdDto(booking.getId(), booking.getBooker().getId());
    }

}
