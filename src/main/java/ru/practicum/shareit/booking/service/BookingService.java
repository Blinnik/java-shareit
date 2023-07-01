package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.model.dto.BookingDto;
import ru.practicum.shareit.booking.model.dto.BookingItemIdAndTimeDto;
import ru.practicum.shareit.booking.model.dto.BookingStatusDto;

import java.util.List;

public interface BookingService {
    BookingDto create(Long userId, BookingItemIdAndTimeDto bookingItemIdAndTimeDto);

    BookingDto updateStatus(Long userId, Long bookingId, Boolean approved);

    BookingDto getById(Long userId, Long bookingId);

    List<BookingDto> getAllByBookerId(Long userId, BookingStatusDto state);

    List<BookingDto> getAllByOwnerId(Long userId, BookingStatusDto state);
}
