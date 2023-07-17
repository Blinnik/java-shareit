package ru.practicum.shareit.booking;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingItemIdAndTimeDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.common.exception.BadRequestException;
import ru.practicum.shareit.common.model.PaginationConfig;

import javax.validation.Valid;
import java.time.LocalDateTime;

@RestController
@RequestMapping(path = "/bookings")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @RequestBody @Valid BookingItemIdAndTimeDto bookingItemIdAndTimeDto) {
        LocalDateTime start = bookingItemIdAndTimeDto.getStart();
        LocalDateTime end = bookingItemIdAndTimeDto.getEnd();

        if (start.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Время начала брони не может быть в прошлом");
        } else if (end.isBefore(start)) {
            throw new BadRequestException("Время конца брони не может быть раньше времени начала");
        } else if (start.equals(end)) {
            throw new BadRequestException("Время конца брони не может совпадать с временем начала");
        }

        log.info("Создаем booking {}, userId={}", bookingItemIdAndTimeDto, userId);
        return bookingClient.create(userId, bookingItemIdAndTimeDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> updateStatus(@RequestHeader("X-Sharer-User-Id") Long userId,
                                               @PathVariable Long bookingId,
                                               @RequestParam Boolean approved) {
        log.info("Обновляем статус booking с id={}, userId={}, approved={}", bookingId, userId, approved);
        return bookingClient.updateStatus(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @PathVariable Long bookingId) {
        log.info("Получаем booking {}, userId={}", bookingId, userId);
        return bookingClient.getById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByBookerId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @RequestParam(defaultValue = "ALL") BookingState state,
                                                   @Valid PaginationConfig paginationConfig) {
        log.info("Получаем все booking с state {}, userId={}, paginationConfig={}", state, userId, paginationConfig);
        return bookingClient.getAllByBookerId(userId, state, paginationConfig);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllByOwnerId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @RequestParam(defaultValue = "ALL") BookingState state,
                                                  @Valid PaginationConfig paginationConfig) {
		log.info("Получаем все booking с state {}, userId={}, paginationConfig={}", state, userId, paginationConfig);
		return bookingClient.getAllByOwnerId(userId, state, paginationConfig);
    }
}
