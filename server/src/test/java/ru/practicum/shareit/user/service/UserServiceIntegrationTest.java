package ru.practicum.shareit.user.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class UserServiceIntegrationTest {
    UserService userService;

    @Test
    void getAll_whenInvoked_thenReturnListOfUsers() {
        UserDto userDto = UserDto.builder().name("test").email("test@mail.com").build();
        UserDto userDto2 = UserDto.builder().name("test2").email("test2@mail.com").build();
        User user = userService.create(userDto);
        User user2 = userService.create(userDto2);

        List<User> actualUsers = userService.getAll();
        List<User> expectedUsers = List.of(user, user2);

        assertEquals(expectedUsers, actualUsers);
    }
}