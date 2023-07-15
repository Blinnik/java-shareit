package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class UserControllerTest {
    @Autowired
    ObjectMapper mapper;

    @MockBean
    UserService userService;

    @Autowired
    MockMvc mvc;

    static final String URL = "/users";
    static final String PATH_VARIABLE_URL = "/users/1";
    static final String USER_NOT_FOUND_ERROR = "Пользователь с id 1 не найден";
    final User.UserBuilder userBuilder = User.builder();
    final UserDto.UserDtoBuilder userDtoBuilder = UserDto.builder();
    User user;
    UserDto userDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("test")
                .email("test@mail.com")
                .build();

        userDto = UserDto.builder()
                .name("test")
                .email("test@mail.com")
                .build();
    }

    @Test
    void create_whenFieldsCorrect_thenReturnUserAndStatusOk() throws Exception {
        when(userService.create(userDto)).thenReturn(user);

        String json = mapper.writeValueAsString(userDto);
        mvc.perform(post(URL)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(user.getName())))
                .andExpect(jsonPath("$.email", is(user.getEmail())));
    }

    @Test
    void create_whenNameNull_thenStatusBadRequest() throws Exception {
        userDto = userDtoBuilder.email("test@mail.com").build();

        String json = mapper.writeValueAsString(userDto);
        mvc.perform(post(URL)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        containsString("create.userDto.name: Имя пользователя не может отсутствовать")));
    }

    @Test
    void create_whenNameLessThan1Char_thenStatusBadRequest() throws Exception {
        userDto = userDtoBuilder.name("").email("test@mail.com").build();

        String json = mapper.writeValueAsString(userDto);
        mvc.perform(post(URL)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("create.userDto.name: " +
                        "Имя пользователя должно содержать от 1 до 150 символов")));
    }

    @Test
    void create_whenNameMoreThan150Chars_thenStatusBadRequest() throws Exception {
        String name = new String(new char[151]).replace('\0', 'N');
        userDto = userDtoBuilder.name(name).email("test@mail.com").build();

        String json = mapper.writeValueAsString(userDto);
        mvc.perform(post(URL)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("create.userDto.name: " +
                        "Имя пользователя должно содержать от 1 до 150 символов")));
    }

    @Test
    void create_whenEmailNull_thenStatusBadRequest() throws Exception {
        userDto = userDtoBuilder.name("test").build();

        String json = mapper.writeValueAsString(userDto);
        mvc.perform(post(URL)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        containsString("create.userDto.email: Email пользователя не может отсутствовать")));
    }

    @Test
    void create_whenEmailNotValid_thenStatusBadRequest() throws Exception {
        userDto = userDtoBuilder.name("test").email("test").build();

        String json = mapper.writeValueAsString(userDto);
        mvc.perform(post(URL)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        containsString("create.userDto.email: Email не соответствует формату")));
    }

    @Test
    void create_whenEmailLessThan1Char_thenStatusBadRequest() throws Exception {
        userDto = userDtoBuilder.name("test").email("").build();

        String json = mapper.writeValueAsString(userDto);
        mvc.perform(post(URL)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("create.userDto.email: " +
                        "Электронная почта должна содержать от 1 до 50 символов")));
    }

    @Test
    void create_whenEmailMoreThan50Chars_thenStatusBadRequest() throws Exception {
        String email = new String(new char[43]).replace('\0', 'N') + "mail.com";

        userDto = userDtoBuilder.name("test").email(email).build();

        String json = mapper.writeValueAsString(userDto);
        mvc.perform(post(URL)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("create.userDto.email: " +
                        "Электронная почта должна содержать от 1 до 50 символов")));
    }

    @Test
    void update_nameWhenNameValid_thenReturnUserAndStatusOk() throws Exception {
        userDto = userDtoBuilder.name("test2").build();
        Long userId = 1L;
        user = userBuilder.id(userId).name("test2").email("test@mail.com").build();

        when(userService.update(userId, userDto)).thenReturn(user);

        String json = mapper.writeValueAsString(userDto);
        mvc.perform(patch(PATH_VARIABLE_URL)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId), Long.class))
                .andExpect(jsonPath("$.name", is(user.getName())))
                .andExpect(jsonPath("$.email", is(user.getEmail())));
    }

    @Test
    void update_emailWhenEmailValid_thenReturnUserAndStatusOk() throws Exception {
        userDto = userDtoBuilder.email("test2@mail.com").build();
        Long userId = 1L;
        user = userBuilder.id(userId).name("test").email("test2@mail.com").build();

        when(userService.update(userId, userDto)).thenReturn(user);

        String json = mapper.writeValueAsString(userDto);
        mvc.perform(patch(PATH_VARIABLE_URL)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId), Long.class))
                .andExpect(jsonPath("$.name", is(user.getName())))
                .andExpect(jsonPath("$.email", is(user.getEmail())));
    }

    @Test
    void update_whenNameAndEmailValid_thenReturnUserAndStatusOk() throws Exception {
        userDto = userDtoBuilder.name("test2").email("test2@mail.com").build();
        Long userId = 1L;
        user = userBuilder.id(userId).name("test2").email("test2@mail.com").build();

        when(userService.update(userId, userDto)).thenReturn(user);

        String json = mapper.writeValueAsString(userDto);
        mvc.perform(patch(PATH_VARIABLE_URL)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId), Long.class))
                .andExpect(jsonPath("$.name", is(user.getName())))
                .andExpect(jsonPath("$.email", is(user.getEmail())));
    }

    @Test
    void update_whenNameLessThan1Char_thenStatusBadRequest() throws Exception {
        userDto = userDtoBuilder.name("").build();

        String json = mapper.writeValueAsString(userDto);
        mvc.perform(patch(PATH_VARIABLE_URL)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("update.userDto.name: " +
                        "Имя пользователя должно содержать от 1 до 150 символов")));
    }

    @Test
    void update_whenNameMoreThan150Chars_thenStatusBadRequest() throws Exception {
        String name = new String(new char[151]).replace('\0', 'N');
        userDto = userDtoBuilder.name(name).build();

        String json = mapper.writeValueAsString(userDto);
        mvc.perform(patch(PATH_VARIABLE_URL)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("update.userDto.name: " +
                        "Имя пользователя должно содержать от 1 до 150 символов")));
    }

    @Test
    void update_whenEmailNotValid_thenStatusBadRequest() throws Exception {
        userDto = userDtoBuilder.email("test").build();

        String json = mapper.writeValueAsString(userDto);
        mvc.perform(patch(PATH_VARIABLE_URL)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        containsString("update.userDto.email: Email не соответствует формату")));
    }

    @Test
    void update_whenEmailLessThan1Char_thenStatusBadRequest() throws Exception {
        userDto = userDtoBuilder.email("").build();

        String json = mapper.writeValueAsString(userDto);
        mvc.perform(patch(PATH_VARIABLE_URL)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("update.userDto.email: " +
                        "Электронная почта должна содержать от 1 до 50 символов")));
    }

    @Test
    void update_whenEmailMoreThan50Chars_thenStatusBadRequest() throws Exception {
        String email = new String(new char[43]).replace('\0', 'N') + "mail.com";
        userDto = userDtoBuilder.email(email).build();

        String json = mapper.writeValueAsString(userDto);
        mvc.perform(patch(PATH_VARIABLE_URL)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("update.userDto.email: " +
                        "Электронная почта должна содержать от 1 до 50 символов")));
    }

    @Test
    void update_whenUserNotFound_thenReturnErrorAndStatusNotFound() throws Exception {
        when(userService.update(1L, userDto)).thenThrow(new NotFoundException(USER_NOT_FOUND_ERROR));

        String json = mapper.writeValueAsString(userDto);
        mvc.perform(patch(PATH_VARIABLE_URL)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(USER_NOT_FOUND_ERROR)));
    }

    @Test
    void getById_whenUserFound_thenReturnUserAndStatusOk() throws Exception {
        when(userService.getById(1L)).thenReturn(user);

        mvc.perform(get(PATH_VARIABLE_URL)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(user.getName())))
                .andExpect(jsonPath("$.email", is(user.getEmail())));
    }

    @Test
    void getById_whenUserNotFound_thenReturnErrorAndStatusNotFound() throws Exception {
        when(userService.getById(1L)).thenThrow(new NotFoundException(USER_NOT_FOUND_ERROR));

        mvc.perform(get(PATH_VARIABLE_URL)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(USER_NOT_FOUND_ERROR)));
    }

    @Test
    void getAll_whenUsersFound_thenReturnUsersAndStatusOk() throws Exception {
        when(userService.getAll()).thenReturn(List.of(user));

        mvc.perform(get(URL)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(user.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(user.getName())))
                .andExpect(jsonPath("$[0].email", is(user.getEmail())));
    }

    @Test
    void getAll_whenUsersNotFound_thenReturnEmptyListAndStatusOk() throws Exception {
        when(userService.getAll()).thenReturn(Collections.emptyList());

        mvc.perform(get(URL)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(0)));
    }

    @Test
    void delete_whenUserFound_thenStatusOk() throws Exception {
        mvc.perform(delete(PATH_VARIABLE_URL))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void delete_whenUserNotFound_thenReturnErrorAndStatusNotFound() throws Exception {
        doThrow(new NotFoundException(USER_NOT_FOUND_ERROR)).when(userService).delete(1L);

        mvc.perform(delete(PATH_VARIABLE_URL)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(USER_NOT_FOUND_ERROR)));
    }
}