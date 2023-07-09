package ru.practicum.shareit.booking.repository;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@FieldDefaults(level = AccessLevel.PRIVATE)
class BookingRepositoryTest {
    @Autowired
    TestEntityManager em;

    @Autowired
    BookingRepository bookingRepository;

    User savedBooker;
    Item savedItem;
    Booking savedBooking;
    Booking savedBooking2;

    @BeforeEach
    void setUp() {
        User savedOwner = User.builder().name("test").email("test@mail.com").build();
        em.persist(savedOwner);

        savedItem = Item.builder()
                .name("Test name")
                .description("Test description")
                .available(true)
                .owner(savedOwner)
                .build();
        em.persist(savedItem);

        savedBooker = User.builder().name("test2").email("test2@mail.com").build();
        em.persist(savedBooker);

        savedBooking = Booking.builder()
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().plusDays(5))
                .end(LocalDateTime.now().plusDays(10))
                .booker(savedBooker)
                .item(savedItem)
                .build();
        em.persist(savedBooking);

        savedBooking2 = Booking.builder()
                .status(BookingStatus.APPROVED)
                .start(LocalDateTime.now().minusDays(10))
                .end(LocalDateTime.now().minusDays(5))
                .booker(savedBooker)
                .item(savedItem)
                .build();
        em.persist(savedBooking2);

        Booking savedBooking3 = Booking.builder()
                .status(BookingStatus.REJECTED)
                .start(LocalDateTime.now().minusDays(5))
                .end(LocalDateTime.now().minusDays(3))
                .booker(savedBooker)
                .item(savedItem)
                .build();
        em.persist(savedBooking3);
    }

    @Test
    void findAllAcceptedByItemId() {
        List<Booking> actualBookingList = bookingRepository.findAllAcceptedByItemId(savedItem.getId());

        assertEquals(List.of(savedBooking, savedBooking2), actualBookingList);
    }

    @Test
    void countAllPrevious() {
        Integer actualCount = bookingRepository.countAllPrevious(savedItem.getId(), savedBooker.getId());

        assertEquals(1, actualCount);
    }
}