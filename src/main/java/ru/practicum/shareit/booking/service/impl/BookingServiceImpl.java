package ru.practicum.shareit.booking.service.impl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.QBooking;
import ru.practicum.shareit.booking.model.dto.BookingItemIdAndTimeDto;
import ru.practicum.shareit.booking.model.dto.BookingStatusDto;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotAvailableException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.NotOwnerException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
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
    public Booking create(Long userId, BookingItemIdAndTimeDto bookingItemIdAndTimeDto) {
        Long itemId = bookingItemIdAndTimeDto.getItemId();
        Item item = itemRepository.findByIdWithOwner(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с id " + itemId + " не найден"));

        if (!item.getAvailable()) {
            throw new NotAvailableException("Предмет не доступен для брони");
        }

        // По тестам требуется выводить ошибку с кодом 404, а не 400, что, кмк, логичнее
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

        return createdBooking;
    }

    @Transactional
    @Override
    public Booking updateStatus(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findByIdWithItem(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронь с id " + bookingId + " не найдена"));

        Long itemId = booking.getItem().getId();
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с id " + itemId + " не найден"));

        if (!Objects.equals(item.getOwner().getId(), userId)) {
            throw new NotOwnerException("Пользователь с id " + userId + " не является владельцем вещи с id " + itemId);
        }

        if (booking.getStatus().equals(APPROVED) && approved ||
                booking.getStatus().equals(REJECTED) && !approved) {
            String message = approved ? "одобрить" : "отклонить";

            throw new NotAvailableException("Нельзя повторно " + message + " бронь");
        }

        if (approved) {
            booking.setStatus(APPROVED);
        } else {
            booking.setStatus(REJECTED);
        }

        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Был обновлен статус брони, id={}", bookingId);

        return updatedBooking;
    }

    @Transactional(readOnly = true)
    @Override
    public Booking getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findByIdWithItem(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронь с id " + bookingId + " не найдена"));

        Long ownerId = booking.getItem().getOwner().getId();
        Long bookerId = booking.getBooker().getId();
        if (!Objects.equals(userId, ownerId) && !Objects.equals(userId, bookerId)) {
            throw new NotFoundException("Нет броней, связанных с пользователем с id " + userId);
        }

        log.info("Получена бронь с id {}: {}", bookingId, booking);

        return booking;
    }

    @Override
    public List<Booking> getAllByBookerId(Long userId, BookingStatusDto state) {
        BooleanExpression byBookerId = qBooking.booker.id.eq(userId);

        try {
            List<Booking> bookings = getAll(state, byBookerId);
            log.info("Получен список броней пользователя с id {}", userId);

            return bookings;
        } catch (NotFoundException e) {
            throw new NotFoundException("По характеристике " + state +
                    " не было найдено вещей, забронированных пользователем с id " + userId);
        }
    }

    @Override
    public List<Booking> getAllByOwnerId(Long userId, BookingStatusDto state) {
        BooleanExpression byOwnerId = qBooking.item.owner.id.eq(userId);

        try {
            List<Booking> bookings = getAll(state, byOwnerId);
            log.info("Получен список броней пользователя с id {}", userId);

            return bookings;
        } catch (NotFoundException e) {
            throw new NotFoundException("По характеристике " + state +
                    " не было найдено вещей, забронированных у пользователя с id " + userId);
        }
    }

    private List<Booking> getAll(BookingStatusDto state, BooleanExpression byId) {
        BooleanExpression byStatus;
        OrderSpecifier<LocalDateTime> byStartDesc = qBooking.start.desc();
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings;

        switch (state) {
            case ALL:
                bookings = (List<Booking>) bookingRepository.findAll(byId, byStartDesc);
                break;
            case CURRENT:
                BooleanExpression byStartBeforeOrEquals = qBooking.start.before(now)
                        .or(qBooking.start.eq(now));
                BooleanExpression byEndAfter = qBooking.end.after(now);

                bookings = (List<Booking>) bookingRepository.findAll(
                        byId.and(byStartBeforeOrEquals).and(byEndAfter),
                        byStartDesc
                );
                break;
            case PAST:
                BooleanExpression byEndBefore = qBooking.end.before(now);

                bookings = (List<Booking>) bookingRepository.findAll(
                        byId.and(byEndBefore),
                        byStartDesc
                );
                break;
            case FUTURE:
                BooleanExpression byStartAfter = qBooking.start.after(now);

                bookings = (List<Booking>) bookingRepository.findAll(
                        byId.and(byStartAfter),
                        byStartDesc
                );
                break;
            case WAITING:
                byStatus = qBooking.status.eq(WAITING);

                bookings = (List<Booking>) bookingRepository.findAll(
                        byId.and(byStatus),
                        byStartDesc
                );
                break;
            case REJECTED:
                byStatus = qBooking.status.eq(REJECTED);

                bookings = (List<Booking>) bookingRepository.findAll(
                        byId.and(byStatus),
                        byStartDesc
                );
                break;
            default:
                throw new NotFoundException();
        }

        if (bookings.isEmpty()) {
            throw new NotFoundException();
        }

        return bookings;
    }
}
