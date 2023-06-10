package ru.practicum.shareit;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.NotOwnerException;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.model.dto.UserDto;

import javax.validation.ValidationException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemTests {
    ItemController itemController;
    UserController userController;

    ItemDto.ItemDtoBuilder itemDtoBuilder = ItemDto.builder()
            .name("Дрель")
            .description("Простая дрель")
            .available(true);
    UserDto.UserDtoBuilder userDtoBuilder = UserDto.builder()
            .name("user")
            .email("user@user.com");

    @Test
    public void create_item() {
        userController.create(userDtoBuilder.build());
        ItemDto returnedItemDto = itemController.create(1L, itemDtoBuilder.build());

        assertEquals(ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Простая дрель")
                .available(true).build(), returnedItemDto);
    }

    @Test
    public void create_failUserIdNotExists() {
        assertThrows(NotFoundException.class,
                () -> itemController.create(99L, itemDtoBuilder.build()));
    }

    @Test
    public void create_failAvailable() {
        assertThrows(ValidationException.class,
                () -> itemController.create(1L, itemDtoBuilder.available(null).build()));
    }

    @Test
    public void create_failName() {
        assertThrows(ValidationException.class,
                () -> itemController.create(1L, itemDtoBuilder.name(null).build()));
    }

    @Test
    public void create_failDescription() {
        assertThrows(ValidationException.class,
                () -> itemController.create(1L, itemDtoBuilder.description(null).build()));
    }

    @Test
    public void update_item() {
        userController.create(userDtoBuilder.build());
        itemController.create(1L, itemDtoBuilder.build());

        ItemDto updatedItemDto = itemDtoBuilder
                .name("Дрель+")
                .description("Аккумуляторная дрель")
                .available(false).build();

        assertEquals(ItemDto.builder()
                .id(1L)
                .name("Дрель+")
                .description("Аккумуляторная дрель")
                .available(false).build(), itemController.update(1L, 1L, updatedItemDto));
    }

    @Test
    public void update_failUserNotOwner() {
        userController.create(userDtoBuilder.build());
        userController.create(userDtoBuilder.email("user2@user.com").build());
        itemController.create(1L, itemDtoBuilder.build());

        ItemDto newItemDto = ItemDto.builder()
                .name("Дрель+")
                .description("Аккумуляторная дрель")
                .available(false)
                .build();

        assertThrows(NotOwnerException.class, () -> itemController.update(2L, 1L, newItemDto));
    }

    @Test
    public void update_itemAvailable() {
        userController.create(userDtoBuilder.build());
        itemController.create(1L, itemDtoBuilder.build());

        ItemDto newItemDto = itemDtoBuilder
                .available(false)
                .build();

        assertEquals(ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Простая дрель")
                .available(false)
                .build(), itemController.update(1L, 1L, newItemDto));
    }

    @Test
    public void update_itemDescription() {
        userController.create(userDtoBuilder.build());
        itemController.create(1L, itemDtoBuilder.build());

        ItemDto newItemDto = itemDtoBuilder
                .description("Дрель + аккумулятор")
                .build();

        assertEquals(ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Дрель + аккумулятор")
                .available(true)
                .build(), itemController.update(1L, 1L, newItemDto));
    }

    @Test
    public void update_itemName() {
        userController.create(userDtoBuilder.build());
        itemController.create(1L, itemDtoBuilder.build());

        ItemDto newItemDto = itemDtoBuilder
                .name("Старая дрель")
                .build();

        assertEquals(ItemDto.builder()
                .id(1L)
                .name("Старая дрель")
                .description("Простая дрель")
                .available(true)
                .build(), itemController.update(1L, 1L, newItemDto));
    }

    @Test
    public void getOne_item() {
        userController.create(userDtoBuilder.build());
        itemController.create(1L, itemDtoBuilder.build());

        assertEquals(ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .build(), itemController.getOne(1L, 1L));
    }

    @Test
    public void create_item2() {
        userController.create(userDtoBuilder.build());
        userController.create(userDtoBuilder.email("another@user.com").build());

        itemController.create(1L, itemDtoBuilder.build());

        ItemDto itemDto = itemDtoBuilder
                .name("Отвертка")
                .description("Аккумуляторная отвертка")
                .available(true)
                .build();

        assertEquals(itemDtoBuilder
                .id(2L)
                .name("Отвертка")
                .description("Аккумуляторная отвертка")
                .available(true)
                .build(), itemController.create(2L, itemDto));
    }

    @Test
    public void getMany_user1() {
        userController.create(userDtoBuilder.build());
        userController.create(userDtoBuilder.email("another@user.com").build());

        itemController.create(1L, itemDtoBuilder.build());
        itemController.create(1L,
                itemDtoBuilder
                        .name("Отвертка")
                        .description("Аккумуляторная отвертка").build());

        itemController.create(2L, itemDtoBuilder
                .name("Кувшин")
                .description("Внутри может сидеть джинн").build());

        assertEquals(List.of(
                itemDtoBuilder
                        .id(1L)
                        .name("Дрель")
                        .description("Простая дрель")
                        .build(),
                itemDtoBuilder
                        .id(2L)
                        .name("Отвертка")
                        .description("Аккумуляторная отвертка")
                        .available(true)
                        .build()
        ), itemController.getMany(1L));
    }

    @Test
    public void getManyByTextQuery_search() {
        userController.create(userDtoBuilder.build());
        userController.create(userDtoBuilder.email("another@user.com").build());

        itemController.create(1L,
                itemDtoBuilder
                        .name("Аккумуляторная дрель")
                        .description("Аккумуляторная дрель + аккумулятор").build());

        itemController.create(2L,
                itemDtoBuilder
                        .name("Отвертка")
                        .description("Аккумуляторная отвертка").build());

        assertEquals(List.of(
                itemDtoBuilder
                        .id(1L)
                        .name("Аккумуляторная дрель")
                        .description("Аккумуляторная дрель + аккумулятор")
                        .available(true)
                        .build(),
                itemDtoBuilder
                        .id(2L)
                        .name("Отвертка")
                        .description("Аккумуляторная отвертка")
                        .available(true)
                        .build()), itemController.getManyByTextQuery(1L, "аккУМУляторная"));
    }

    @Test
    public void getManyByTextQuery_sameSearchButOneItemHasNoAvailability() {
        userController.create(userDtoBuilder.build());
        userController.create(userDtoBuilder.email("another@user.com").build());

        itemController.create(1L,
                itemDtoBuilder
                        .name("Аккумуляторная дрель")
                        .description("Аккумуляторная дрель + аккумулятор").build());

        itemController.create(2L,
                itemDtoBuilder
                        .name("Отвертка")
                        .description("Аккумуляторная отвертка")
                        .available(false).build());

        assertEquals(List.of(
                itemDtoBuilder
                        .id(1L)
                        .name("Аккумуляторная дрель")
                        .description("Аккумуляторная дрель + аккумулятор")
                        .available(true)
                        .build()
        ), itemController.getManyByTextQuery(1L, "аккУМУляторная"));
    }

    @Test
    public void getManyByTextQuery_anotherSearch() {
        userController.create(userDtoBuilder.build());
        userController.create(userDtoBuilder.email("another@user.com").build());

        itemController.create(1L,
                itemDtoBuilder
                        .name("Аккумуляторная дрель")
                        .description("Аккумуляторная дрель + аккумулятор").build());

        itemController.create(2L,
                itemDtoBuilder
                        .name("Отвертка")
                        .description("Аккумуляторная отвертка").build());

        assertEquals(List.of(
                ItemDto.builder()
                        .id(2L)
                        .name("Отвертка")
                        .description("Аккумуляторная отвертка")
                        .available(true)
                        .build()), itemController.getManyByTextQuery(1L, "оТверТ"));
    }

    @Test
    public void getManyByTextQuery_searchEmpty() {
        userController.create(userDtoBuilder.build());
        userController.create(userDtoBuilder.email("another@user.com").build());

        itemController.create(1L, itemDtoBuilder.build());

        itemController.create(2L,
                itemDtoBuilder
                        .name("Отвертка")
                        .description("Аккумуляторная отвертка").build());

        assertEquals(List.of(), itemController.getManyByTextQuery(1L, ""));
    }
}
