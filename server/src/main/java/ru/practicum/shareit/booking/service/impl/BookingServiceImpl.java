package ru.practicum.shareit.booking.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.QBooking;
import ru.practicum.shareit.booking.model.dto.BookingDto;
import ru.practicum.shareit.booking.model.dto.BookingItemIdAndTimeDto;
import ru.practicum.shareit.booking.model.dto.BookingState;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.common.exception.BadRequestException;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.common.model.PaginationConfig;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static ru.practicum.shareit.booking.model.BookingStatus.*;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class BookingServiceImpl implements BookingService {
    BookingRepository bookingRepository;
    ItemRepository itemRepository;
    UserRepository userRepository;
    QBooking qBooking = QBooking.booking;

    @Transactional
    @Override
    public BookingDto create(Long userId, BookingItemIdAndTimeDto bookingItemIdAndTimeDto) {
        Long itemId = bookingItemIdAndTimeDto.getItemId();
        Item item = itemRepository.findByIdWithOwner(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с id " + itemId + " не найден"));

        if (!item.getAvailable()) {
            throw new BadRequestException("Предмет не доступен для брони");
        }

        if (Objects.equals(item.getOwner().getId(), userId)) {
            throw new NotFoundException("Пользователь не может забронировать собственный предмет");
        }

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Booking booking = BookingMapper.toBooking(userId, bookingItemIdAndTimeDto);
        booking.setItem(item);
        booking.setBooker(booker);

        Booking createdBooking = bookingRepository.save(booking);
        log.info("Была добавлена бронь, id={}", createdBooking.getId());

        return BookingMapper.toBookingDto(createdBooking);
    }

    @Transactional
    @Override
    public BookingDto updateStatus(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронь с id " + bookingId + " не найдена"));

        Long itemId = booking.getItem().getId();
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с id " + itemId + " не найден"));

        if (!Objects.equals(item.getOwner().getId(), userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не является владельцем вещи с id " + itemId);
        }

        if (booking.getStatus().equals(APPROVED) && approved ||
                booking.getStatus().equals(REJECTED) && !approved) {
            String message = approved ? "одобрить" : "отклонить";

            throw new BadRequestException("Нельзя повторно " + message + " бронь");
        }

        if (approved) {
            booking.setStatus(APPROVED);
        } else {
            booking.setStatus(REJECTED);
        }

        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Был обновлен статус брони, id={}", bookingId);

        return BookingMapper.toBookingDto(updatedBooking);
    }

    @Transactional(readOnly = true)
    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронь с id " + bookingId + " не найдена"));

        Long ownerId = booking.getItem().getOwner().getId();
        Long bookerId = booking.getBooker().getId();
        if (!Objects.equals(userId, ownerId) && !Objects.equals(userId, bookerId)) {
            throw new NotFoundException("Нет броней, связанных с пользователем с id " + userId);
        }

        log.info("Получена бронь с id {}: {}", bookingId, booking);

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getAllByBookerId(Long userId,
                                             BookingState state,
                                             PaginationConfig paginationConfig) {
        BooleanExpression byBookerId = qBooking.booker.id.eq(userId);

        try {
            List<BookingDto> bookings = getAll(state, byBookerId, paginationConfig);
            log.info("Получен список броней пользователя с id {}", userId);

            return bookings;
        } catch (NotFoundException e) {
            throw new NotFoundException("По характеристике " + state +
                    " не было найдено вещей, забронированных пользователем с id " + userId);
        }
    }

    @Override
    public List<BookingDto> getAllByOwnerId(Long userId,
                                            BookingState state,
                                            PaginationConfig paginationConfig) {
        BooleanExpression byOwnerId = qBooking.item.owner.id.eq(userId);

        try {
            List<BookingDto> bookings = getAll(state, byOwnerId, paginationConfig);
            log.info("Получен список броней пользователя с id {}", userId);

            return bookings;
        } catch (NotFoundException e) {
            throw new NotFoundException("По характеристике " + state +
                    " не было найдено вещей, забронированных у пользователя с id " + userId);
        }
    }

    private List<BookingDto> getAll(BookingState state,
                                    BooleanExpression byId,
                                    PaginationConfig paginationConfig) {
        LocalDateTime now = LocalDateTime.now();

        List<BooleanExpression> expressions = new ArrayList<>();

        expressions.add(byId);

        BooleanExpression byStatus;

        switch (state) {
            case CURRENT:
                BooleanExpression byStartBeforeOrEquals = qBooking.start.before(now)
                        .or(qBooking.start.eq(now));
                BooleanExpression byEndAfter = qBooking.end.after(now);

                expressions.add(byStartBeforeOrEquals);
                expressions.add(byEndAfter);

                break;
            case PAST:
                BooleanExpression byEndBefore = qBooking.end.before(now);
                expressions.add(byEndBefore);
                break;
            case FUTURE:
                BooleanExpression byStartAfter = qBooking.start.after(now);
                expressions.add(byStartAfter);

                break;
            case WAITING:
                byStatus = qBooking.status.eq(WAITING);
                expressions.add(byStatus);

                break;
            case REJECTED:
                byStatus = qBooking.status.eq(REJECTED);
                expressions.add(byStatus);

                break;
        }

        BooleanExpression finalExpression = expressions.stream().reduce(BooleanExpression::and).get();

        Sort byStartDesc = Sort.by("start").descending();
        Pageable pageable = paginationConfig.getPageable(byStartDesc);

        List<Booking> bookings = bookingRepository.findAll(finalExpression, pageable).getContent();

        if (bookings.isEmpty()) {
            throw new NotFoundException();
        }

        return BookingMapper.toBookingDto(bookings);
    }
}
