package ru.practicum.shareit.booking.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.dto.BookingDto;
import ru.practicum.shareit.booking.model.dto.BookingItemIdAndTimeDto;
import ru.practicum.shareit.booking.model.dto.BookingStatusDto;
import ru.practicum.shareit.common.model.PaginationConfig;
import ru.practicum.shareit.item.model.dto.ItemRequestIdDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class BookingServiceIntegrationTest {
    BookingService bookingService;
    ItemService itemService;
    UserService userService;

    PaginationConfig paginationConfig = new PaginationConfig();

    @Test
    void getAllByBookerId() {
        // Создаем владельца вещей и сами вещи
        UserDto userDto = UserDto.builder().name("test").email("test@mail.com").build();
        User owner = userService.create(userDto);
        Long ownerId = owner.getId();

        ItemRequestIdDto itemRequestIdDto = ItemRequestIdDto.builder()
                .name("test item name")
                .description("test item description")
                .available(true)
                .build();

        ItemRequestIdDto itemRequestIdDto2 = ItemRequestIdDto.builder()
                .name("test item name2")
                .description("test item description2")
                .available(true)
                .build();

        itemRequestIdDto = itemService.create(ownerId, itemRequestIdDto);
        itemRequestIdDto2 = itemService.create(ownerId, itemRequestIdDto2);

        // Создаем пользователей, которые будут брать в аренду вещи
        UserDto userDto2 = UserDto.builder().name("test2").email("test2@mail.com").build();
        User booker = userService.create(userDto2);
        Long bookerId = booker.getId();

        UserDto userDto3 = UserDto.builder().name("test3").email("test3@mail.com").build();
        User booker2 = userService.create(userDto3);
        Long booker2Id = booker2.getId();

        // Создаем брони
        BookingItemIdAndTimeDto bookingItemIdAndTimeDto = new BookingItemIdAndTimeDto(
                itemRequestIdDto.getId(),
                LocalDateTime.now().plusDays(1).toString(),
                LocalDateTime.now().plusDays(2).toString()
        );

        BookingItemIdAndTimeDto bookingItemIdAndTimeDto2 = new BookingItemIdAndTimeDto(
                itemRequestIdDto.getId(),
                LocalDateTime.now().plusDays(10).toString(),
                LocalDateTime.now().plusDays(20).toString()
        );

        BookingItemIdAndTimeDto bookingItemIdAndTimeDto3 = new BookingItemIdAndTimeDto(
                itemRequestIdDto2.getId(),
                LocalDateTime.now().plusDays(4).toString(),
                LocalDateTime.now().plusDays(5).toString()
        );

        BookingDto bookingDto = bookingService.create(bookerId, bookingItemIdAndTimeDto);
        BookingDto bookingDto2 = bookingService.create(bookerId, bookingItemIdAndTimeDto2);
        BookingDto bookingDto3 = bookingService.create(booker2Id, bookingItemIdAndTimeDto3);

        List<BookingDto> expectedBookingDtoListOfBooker1 =
                bookingService.getAllByBookerId(bookerId, BookingStatusDto.ALL, paginationConfig);

        // Меняем статус
        bookingService.updateStatus(ownerId, bookingDto.getId(), true);

        List<BookingDto> expectedBookingDtoListOfBooker1WhenStatusUpdated =
                bookingService.getAllByBookerId(bookerId, BookingStatusDto.WAITING, paginationConfig);

        List<BookingDto> expectedBookingDtoListOfBooker2 =
                bookingService.getAllByBookerId(booker2Id, BookingStatusDto.ALL, paginationConfig);


        assertEquals(2, expectedBookingDtoListOfBooker1.size());
        assertEquals(bookingDto2.getId(), expectedBookingDtoListOfBooker1.get(0).getId());
        assertEquals(bookingDto2.getBooker().getId(), expectedBookingDtoListOfBooker1.get(0).getBooker().getId());
        assertEquals(bookingDto2.getItem(), expectedBookingDtoListOfBooker1.get(0).getItem());
        assertEquals(bookingDto.getId(), expectedBookingDtoListOfBooker1.get(1).getId());
        assertEquals(bookingDto.getBooker().getId(), expectedBookingDtoListOfBooker1.get(1).getBooker().getId());
        assertEquals(bookingDto.getItem(), expectedBookingDtoListOfBooker1.get(1).getItem());

        assertEquals(1, expectedBookingDtoListOfBooker1WhenStatusUpdated.size());
        assertEquals(bookingDto2.getId(), expectedBookingDtoListOfBooker1WhenStatusUpdated.get(0).getId());
        assertEquals(bookingDto2.getBooker().getId(),
                expectedBookingDtoListOfBooker1WhenStatusUpdated.get(0).getBooker().getId());
        assertEquals(bookingDto2.getItem(), expectedBookingDtoListOfBooker1WhenStatusUpdated.get(0).getItem());

        assertEquals(1, expectedBookingDtoListOfBooker2.size());
        assertEquals(bookingDto3.getId(), expectedBookingDtoListOfBooker2.get(0).getId());
        assertEquals(bookingDto3.getBooker().getId(), expectedBookingDtoListOfBooker2.get(0).getBooker().getId());
        assertEquals(bookingDto3.getItem(), expectedBookingDtoListOfBooker2.get(0).getItem());
    }
}