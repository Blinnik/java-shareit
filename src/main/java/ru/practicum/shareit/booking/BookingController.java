package ru.practicum.shareit.booking;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.dto.BookingStatusDto;
import ru.practicum.shareit.booking.model.dto.BookingItemIdAndTimeDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotValidException;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Validated
public class BookingController {
    BookingService bookingService;

    @PostMapping
    public Booking create(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @RequestBody @Valid BookingItemIdAndTimeDto bookingItemIdAndTimeDto) {
        LocalDateTime start = LocalDateTime.parse(bookingItemIdAndTimeDto.getStart());
        LocalDateTime end = LocalDateTime.parse(bookingItemIdAndTimeDto.getEnd());

        if (start.isBefore(LocalDateTime.now())) {
            throw new NotValidException("Время начала брони не может быть в прошлом");
        } else if (end.isBefore(start)) {
            throw new NotValidException("Время конца брони не может быть раньше времени начала");
        } else if (end.equals(start)) {
            throw new NotValidException("Время конца брони не может совпадать с временем начала");
        }

        return bookingService.create(userId, bookingItemIdAndTimeDto);
    }

    @PatchMapping("/{bookingId}")
    public Booking updateStatus(@RequestHeader("X-Sharer-User-Id") Long userId,
                                @PathVariable Long bookingId,
                                @RequestParam Boolean approved) {
        return bookingService.updateStatus(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public Booking getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                           @PathVariable Long bookingId) {
        return bookingService.getById(userId, bookingId);
    }

    @GetMapping
    public List<Booking> getAllByBookerId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @RequestParam(defaultValue = "ALL") BookingStatusDto state) {
        return bookingService.getAllByBookerId(userId, state);
    }

    @GetMapping("/owner")
    public List<Booking> getAllByOwnerId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestParam(defaultValue = "ALL") BookingStatusDto state) {
        return bookingService.getAllByOwnerId(userId, state);
    }
}