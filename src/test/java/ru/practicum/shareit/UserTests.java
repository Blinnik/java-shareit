package ru.practicum.shareit;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.AlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.dto.UserDto;

import javax.validation.ValidationException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserTests {
    UserController userController;
    UserDto.UserDtoBuilder userDtoBuilder = UserDto.builder()
            .name("user")
            .email("user@user.com");

    @Test
    public void create_user() {
        User returnedUser = userController.create(userDtoBuilder.build());

        assertEquals(User.builder()
                .id(1L)
                .name("user")
                .email("user@user.com")
                .build(), returnedUser);
    }

    @Test
    public void create_failDuplicateEmail() {
        userController.create(userDtoBuilder.build());

        assertThrows(AlreadyExistsException.class,
                () -> userController.create(userDtoBuilder.build()));
    }

    @Test
    public void create_failNoEmail() {
        UserDto userDto = userDtoBuilder.email(null).build();

        assertThrows(ValidationException.class, () -> userController.create(userDto));
    }

    @Test
    public void create_failInvalidEmail() {
        UserDto userDto = userDtoBuilder.email("user.com").build();

        assertThrows(ValidationException.class, () -> userController.create(userDto));
    }

    @Test
    public void update_user() {
        userController.create(userDtoBuilder.build());

        UserDto newUserDto = userDtoBuilder.email("update@user.com").build();

        User updatedUser = userController.update(1L, newUserDto);

        assertEquals(User.builder()
                .id(1L)
                .name("user")
                .email("update@user.com")
                .build(), updatedUser);
    }

    @Test
    public void create_user2() {
        userController.create(userDtoBuilder.build());
        User createdUser2 = userController.create(userDtoBuilder.email("user2@user.com").build());

        assertEquals(User.builder()
                .id(2L)
                .name("user")
                .email("user2@user.com")
                .build(), createdUser2);
    }

    @Test
    public void update_userName() {
        userController.create(userDtoBuilder.build());

        User updatedUser = userController.update(1L,
                userDtoBuilder.name("updatedUser").build());

        assertEquals(User.builder()
                .id(1L)
                .name("updatedUser")
                .email("user@user.com")
                .build(), updatedUser);
    }

    @Test
    public void update_userEmail() {
        userController.create(userDtoBuilder.build());

        User updatedUser = userController.update(1L,
                userDtoBuilder.email("updated_user@user.com").build());

        assertEquals(User.builder()
                .id(1L)
                .name("user")
                .email("updated_user@user.com")
                .build(), updatedUser);
    }

    @Test
    public void update_failEmailExists() {
        userController.create(userDtoBuilder.build());

        assertThrows(AlreadyExistsException.class,
                () -> userController.create(userDtoBuilder.build()));
    }

    @Test
    public void getOne_user() {
        userController.create(userDtoBuilder.build());
        userController.create(userDtoBuilder.email("another@user.com").build());

        assertEquals(User.builder()
                .id(2L)
                .name("user")
                .email("another@user.com")
                .build(), userController.getOne(2L));
    }

    @Test
    public void delete_user() {
        userController.create(userDtoBuilder.build());
        userController.delete(1L);
        assertThrows(NotFoundException.class, () -> userController.getOne(1L));
    }

    @Test
    public void create_afterDelete() {
        userController.create(userDtoBuilder.build());
        userController.delete(1L);

        User returnedUser = userController.create(userDtoBuilder.build());

        assertEquals(User.builder()
                .id(2L)
                .name("user")
                .email("user@user.com")
                .build(), returnedUser);
    }

    @Test
    public void getMany() {
        userController.create(userDtoBuilder.build());
        userController.create(userDtoBuilder.email("another@user.com").build());

        List<User> users = List.of(
                User.builder().id(1L).name("user").email("user@user.com").build(),
                User.builder().id(2L).name("user").email("another@user.com").build()
        );

        assertEquals(2, users.size());
        assertEquals(users, userController.getMany());
    }
}
