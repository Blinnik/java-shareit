package ru.practicum.shareit.item.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.dto.BookingBookerIdDto;
import ru.practicum.shareit.booking.model.dto.BookingDto;
import ru.practicum.shareit.booking.model.dto.BookingItemIdAndTimeDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.common.model.PaginationConfig;
import ru.practicum.shareit.item.model.dto.ItemBookingsDto;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.item.model.dto.ItemRequestIdDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class ItemServiceIntegrationTest {

    ItemService itemService;
    UserService userService;
    BookingService bookingService;

    PaginationConfig paginationConfig = new PaginationConfig();

    @Test
    void getAllByOwnerId_whenUserFound_thenReturnUserItems() {
        // GIVEN
        // Создаем пользователей
        UserDto userDto = UserDto.builder().name("test").email("test@mail.com").build();
        UserDto userDto2 = UserDto.builder().name("test2").email("test2@mail.com").build();
        User user = userService.create(userDto);
        User user2 = userService.create(userDto2);
        Long user1Id = user.getId();
        Long user2Id = user2.getId();

        // Создаем предметы для пользователя 1
        ItemRequestIdDto itemRequestIdDto = ItemRequestIdDto.builder()
                .name("test item name")
                .description("test item description")
                .available(true)
                .build();

        ItemRequestIdDto itemRequestIdDto2 = ItemRequestIdDto.builder()
                .name("test item name2")
                .description("test item description2")
                .available(false)
                .build();

        itemRequestIdDto = itemService.create(user1Id, itemRequestIdDto);
        itemRequestIdDto2 = itemService.create(user1Id, itemRequestIdDto2);

        // Создаем предметы для пользователя 2
        ItemRequestIdDto itemRequestIdDto3 = ItemRequestIdDto.builder()
                .name("test item name3")
                .description("test item description3")
                .available(true)
                .build();

        ItemRequestIdDto itemRequestIdDto4 = ItemRequestIdDto.builder()
                .name("test item name4")
                .description("test item description4")
                .available(true)
                .build();

        itemRequestIdDto3 = itemService.create(user2Id, itemRequestIdDto3);
        itemRequestIdDto4 = itemService.create(user2Id, itemRequestIdDto4);

        // Создаем бронирование
        BookingItemIdAndTimeDto bookingItemIdAndTimeDto = new BookingItemIdAndTimeDto(
                itemRequestIdDto4.getId(),
                LocalDateTime.now().minusDays(1).toString(),
                LocalDateTime.now().plusDays(1).toString()
        );

        BookingItemIdAndTimeDto bookingItemIdAndTimeDto2 = new BookingItemIdAndTimeDto(
                itemRequestIdDto.getId(),
                LocalDateTime.now().plusDays(10).toString(),
                LocalDateTime.now().plusDays(20).toString()
        );

        BookingDto bookingDto = bookingService.create(user1Id, bookingItemIdAndTimeDto);
        BookingDto bookingDto2 = bookingService.create(user2Id, bookingItemIdAndTimeDto2);

        // WHEN
        List<ItemBookingsDto> actualUser1ItemBookingsDtos = itemService.getAllByOwnerId(user1Id, paginationConfig);
        List<ItemBookingsDto> actualUser2ItemBookingsDtos = itemService.getAllByOwnerId(user2Id, paginationConfig);

        // THEN
        List<ItemBookingsDto> expectedUser1ItemBookingsDtos = List.of(
                new ItemBookingsDto(
                        itemRequestIdDto.getId(),
                        itemRequestIdDto.getName(),
                        itemRequestIdDto.getDescription(),
                        itemRequestIdDto.getAvailable(),
                        null,
                        new BookingBookerIdDto(bookingDto2.getId(), user2Id)
                ),
                new ItemBookingsDto(
                        itemRequestIdDto2.getId(),
                        itemRequestIdDto2.getName(),
                        itemRequestIdDto2.getDescription(),
                        itemRequestIdDto2.getAvailable(),
                        null,
                        null
                )
        );

        List<ItemBookingsDto> expectedUser2ItemBookingsDtos = List.of(
                new ItemBookingsDto(
                        itemRequestIdDto3.getId(),
                        itemRequestIdDto3.getName(),
                        itemRequestIdDto3.getDescription(),
                        itemRequestIdDto3.getAvailable(),
                        null,
                        null
                ),
                new ItemBookingsDto(
                        itemRequestIdDto4.getId(),
                        itemRequestIdDto4.getName(),
                        itemRequestIdDto4.getDescription(),
                        itemRequestIdDto4.getAvailable(),
                        new BookingBookerIdDto(bookingDto.getId(), user1Id),
                        null
                )
        );

        assertEquals(expectedUser1ItemBookingsDtos, actualUser1ItemBookingsDtos);
        assertEquals(expectedUser2ItemBookingsDtos, actualUser2ItemBookingsDtos);
    }

    @Test
    void getAllByTextQuery_whenUserFound_thenReturnSearchableItems() {
        // GIVEN
        // Создаем пользователей
        UserDto userDto = UserDto.builder().name("test").email("test@mail.com").build();
        UserDto userDto2 = UserDto.builder().name("test2").email("test2@mail.com").build();
        User user = userService.create(userDto);
        User user2 = userService.create(userDto2);
        Long user1Id = user.getId();
        Long user2Id = user2.getId();

        // Создаем предметы для пользователя 1
        ItemRequestIdDto itemRequestIdDto = ItemRequestIdDto.builder()
                .name("Дрель")
                .description("test item description")
                .available(true)
                .build();

        ItemRequestIdDto itemRequestIdDto2 = ItemRequestIdDto.builder()
                .name("test item name2")
                .description("Самокат")
                .available(false)
                .build();

        itemRequestIdDto = itemService.create(user1Id, itemRequestIdDto);
        itemService.create(user1Id, itemRequestIdDto2);

        // Создаем предметы для пользователя 2
        ItemRequestIdDto itemRequestIdDto3 = ItemRequestIdDto.builder()
                .name("Пылесос")
                .description("test item description3")
                .available(true)
                .build();

        ItemRequestIdDto itemRequestIdDto4 = ItemRequestIdDto.builder()
                .name("test item name4")
                .description("Шуруповёрт")
                .available(true)
                .build();

        itemService.create(user2Id, itemRequestIdDto3);
        itemRequestIdDto4 = itemService.create(user2Id, itemRequestIdDto4);

        // WHEN
        List<ItemDto> actualUser1ItemDtos = itemService.getAllByTextQuery(user1Id, "шуруП", paginationConfig);
        List<ItemDto> actualUser2ItemDtos = itemService.getAllByTextQuery(user2Id, "дре", paginationConfig);

        // THEN
        List<ItemDto> expectedUser1ItemDtos = List.of(
                ItemDto.builder()
                        .id(itemRequestIdDto4.getId())
                        .name(itemRequestIdDto4.getName())
                        .description(itemRequestIdDto4.getDescription())
                        .available(itemRequestIdDto4.getAvailable())
                        .build()
        );

        List<ItemDto> expectedUser2ItemDtos = List.of(
                ItemDto.builder()
                        .id(itemRequestIdDto.getId())
                        .name(itemRequestIdDto.getName())
                        .description(itemRequestIdDto.getDescription())
                        .available(itemRequestIdDto.getAvailable())
                        .build()
        );

        assertEquals(expectedUser1ItemDtos, actualUser1ItemDtos);
        assertEquals(expectedUser2ItemDtos, actualUser2ItemDtos);
    }
}