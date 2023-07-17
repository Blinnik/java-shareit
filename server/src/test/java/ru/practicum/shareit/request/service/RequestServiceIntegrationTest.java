package ru.practicum.shareit.request.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.common.model.PaginationConfig;
import ru.practicum.shareit.item.model.dto.ItemRequestIdDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.model.dto.RequestDescriptionDto;
import ru.practicum.shareit.request.model.dto.RequestDto;
import ru.practicum.shareit.request.model.dto.RequestItemsDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class RequestServiceIntegrationTest {
    RequestService requestService;
    UserService userService;
    ItemService itemService;

    @Test
    void getAll() {
        UserDto userDto = UserDto.builder().name("test").email("test@mail.com").build();
        User user = userService.create(userDto);
        Long userId = user.getId();

        RequestDto requestDto = requestService.create(userId, new RequestDescriptionDto("Test description"));
        RequestDto requestDto2 = requestService.create(userId, new RequestDescriptionDto("Test description2"));

        ItemRequestIdDto itemRequestIdDto = ItemRequestIdDto.builder()
                .name("test item name")
                .description("test item description")
                .available(true)
                .requestId(requestDto.getId())
                .build();

        ItemRequestIdDto itemRequestIdDto2 = ItemRequestIdDto.builder()
                .name("test item name2")
                .description("test item description2")
                .available(false)
                .requestId(requestDto2.getId())
                .build();

        UserDto userDto2 = UserDto.builder().name("test2").email("test2@mail.com").build();
        User user2 = userService.create(userDto2);
        Long user2Id = user2.getId();

        itemRequestIdDto = itemService.create(user2Id, itemRequestIdDto);
        itemRequestIdDto2 = itemService.create(user2Id, itemRequestIdDto2);

        List<RequestItemsDto> actualRequestItemsDtos = requestService.getAll(user2Id, new PaginationConfig());


        assertEquals(2, actualRequestItemsDtos.size());

        assertEquals(requestDto2.getId(), actualRequestItemsDtos.get(0).getId());
        assertEquals(requestDto2.getDescription(), actualRequestItemsDtos.get(0).getDescription());
        assertEquals(List.of(itemRequestIdDto2), actualRequestItemsDtos.get(0).getItems());

        assertEquals(requestDto.getId(), actualRequestItemsDtos.get(1).getId());
        assertEquals(requestDto.getDescription(), actualRequestItemsDtos.get(1).getDescription());
        assertEquals(List.of(itemRequestIdDto), actualRequestItemsDtos.get(1).getItems());
    }
}