package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.dto.BookingStatusDto;
import ru.practicum.shareit.booking.model.dto.BookingItemIdAndTimeDto;

import java.util.List;

public interface BookingService {
    Booking create(Long userId, BookingItemIdAndTimeDto bookingItemIdAndTimeDto);

    Booking updateStatus(Long userId, Long bookingId, Boolean approved);

    Booking getById(Long userId, Long bookingId);

    List<Booking> getAllByBookerId(Long userId, BookingStatusDto state);

    List<Booking> getAllByOwnerId(Long userId, BookingStatusDto state);
}
