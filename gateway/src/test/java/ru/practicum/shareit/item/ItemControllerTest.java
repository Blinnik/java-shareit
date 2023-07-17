package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingBookerIdDto;
import ru.practicum.shareit.common.exception.NotAvailableException;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.common.exception.NotOwnerException;
import ru.practicum.shareit.common.model.PaginationConfig;
import ru.practicum.shareit.item.dto.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemControllerTest {
    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemClient itemClient;

    @Autowired
    MockMvc mvc;

    static final String URL = "/items";
    static final String PATH_VARIABLE_URL = "/items/1";
    static final String SEARCH_URL = "/items/search";
    static final String COMMENT_URL = "/items/1/comment";
    static final String USER_NOT_FOUND_ERROR = "Пользователь с id 1 не найден";
    static final String REQUEST_NOT_FOUND_ERROR = "Запрос с id 1 не найден";
    static final String ITEM_NOT_FOUND_ERROR = "Предмет с id 1 не найден";
    static final String NOT_OWNER_ERROR = "Пользователь с id 1 не является владельцем предмета с id 1";
    final BookingBookerIdDto lastBooking = new BookingBookerIdDto(1L, 1L);
    final BookingBookerIdDto nextBooking = new BookingBookerIdDto(2L, 2L);
    ItemRequestIdDto itemRequestIdDto;
    ItemDto itemDto;
    ItemBookingsAndCommentsDto itemBookingsAndCommentsDto;
    ItemBookingsDto itemBookingsDto;
    CommentTextDto commentTextDto;
    CommentDto commentDto;

    @BeforeEach
    void setUp() {
        itemRequestIdDto = ItemRequestIdDto.builder()
                .id(1L)
                .name("Test name")
                .description("Test description")
                .available(true)
                .requestId(1L)
                .build();

        itemDto = ItemDto.builder()
                .id(1L)
                .name("Test name")
                .description("Test description")
                .available(true)
                .build();

        itemBookingsDto = new ItemBookingsDto(
                1L,
                "Test name",
                "Test description",
                true,
                lastBooking,
                nextBooking
        );

        commentTextDto = new CommentTextDto("Comment text");

        commentDto = new CommentDto(
                1L,
                "text",
                "author name",
                LocalDateTime.now()
        );

        itemBookingsAndCommentsDto = ItemBookingsAndCommentsDto.builder()
                .id(1L)
                .name("Test name")
                .description("Test description")
                .available(true)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(List.of(commentDto))
                .build();
    }

    @Test
    void create_whenItemRequestIdDtoCorrectAndUserAndRequestFound_thenReturnItemRequestIdDtoAndStatusOk() throws Exception {
        ResponseEntity<Object> response = ResponseEntity.ok().body(itemRequestIdDto);

        when(itemClient.create(1L, itemRequestIdDto)).thenReturn(response);

        String json = mapper.writeValueAsString(itemRequestIdDto);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestIdDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestIdDto.getDescription())))
                .andExpect(jsonPath("$.name", is(itemRequestIdDto.getName())))
                .andExpect(jsonPath("$.available", is(itemRequestIdDto.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemRequestIdDto.getRequestId()), Long.class));
    }

    @Test
    void create_whenUserNotFound_thenReturnErrorAndStatusNotFound() throws Exception {
        when(itemClient.create(1L, itemRequestIdDto))
                .thenThrow(new NotFoundException(USER_NOT_FOUND_ERROR));

        String json = mapper.writeValueAsString(itemRequestIdDto);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(USER_NOT_FOUND_ERROR)));
    }

    @Test
    void create_whenRequestNotFound_thenReturnErrorAndStatusNotFound() throws Exception {
        when(itemClient.create(1L, itemRequestIdDto))
                .thenThrow(new NotFoundException(REQUEST_NOT_FOUND_ERROR));

        String json = mapper.writeValueAsString(itemRequestIdDto);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(REQUEST_NOT_FOUND_ERROR)));
    }

    @Test
    void create_whenNameNull_thenReturnErrorAndStatusBadRequest() throws Exception {
        ItemRequestIdDto itemRequestIdDto = ItemRequestIdDto.builder()
                .id(1L)
                .name(null)
                .description("Test description")
                .available(true)
                .requestId(1L)
                .build();

        String json = mapper.writeValueAsString(itemRequestIdDto);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        containsString("create.itemRequestIdDto.name:" +
                                " Название предмета не может отсутствовать")));
    }

    @Test
    void create_whenNameLessThan1Char_thenReturnErrorAndStatusBadRequest() throws Exception {
        ItemRequestIdDto itemRequestIdDto = ItemRequestIdDto.builder()
                .id(1L)
                .name("")
                .description("Test description")
                .available(true)
                .requestId(1L)
                .build();

        String json = mapper.writeValueAsString(itemRequestIdDto);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        containsString("create.itemRequestIdDto.name:" +
                                " Название предмета должно содержать от 1 до 200 символов")));
    }

    @Test
    void create_whenNameMoreThan200Chars_thenReturnErrorAndStatusBadRequest() throws Exception {
        String badName = new String(new char[201]).replace('\0', 'N');
        ItemRequestIdDto itemRequestIdDto = ItemRequestIdDto.builder()
                .id(1L)
                .name(badName)
                .description("Test description")
                .available(true)
                .requestId(1L)
                .build();

        String json = mapper.writeValueAsString(itemRequestIdDto);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        containsString("create.itemRequestIdDto.name:" +
                                " Название предмета должно содержать от 1 до 200 символов")));
    }

    @Test
    void create_whenDescriptionNull_thenReturnErrorAndStatusBadRequest() throws Exception {
        ItemRequestIdDto itemRequestIdDto = ItemRequestIdDto.builder()
                .id(1L)
                .name("Test name")
                .description(null)
                .available(true)
                .requestId(1L)
                .build();

        String json = mapper.writeValueAsString(itemRequestIdDto);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        containsString("create.itemRequestIdDto.description:" +
                                " Описание предмета не может отсутствовать")));
    }

    @Test
    void create_whenDescriptionLessThan1Char_thenReturnErrorAndStatusBadRequest() throws Exception {
        ItemRequestIdDto itemRequestIdDto = ItemRequestIdDto.builder()
                .id(1L)
                .name("Test name")
                .description("")
                .available(true)
                .requestId(1L)
                .build();

        String json = mapper.writeValueAsString(itemRequestIdDto);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        containsString("create.itemRequestIdDto.description:" +
                                " Описание предмета должно содержать от 1 до 1000 символов")));
    }

    @Test
    void create_whenDescriptionMoreThan1000Chars_thenReturnErrorAndStatusBadRequest() throws Exception {
        String badDescription = new String(new char[1001]).replace('\0', 'N');
        ItemRequestIdDto itemRequestIdDto = ItemRequestIdDto.builder()
                .id(1L)
                .name("Test name")
                .description(badDescription)
                .available(true)
                .requestId(1L)
                .build();

        String json = mapper.writeValueAsString(itemRequestIdDto);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        containsString("create.itemRequestIdDto.description:" +
                                " Описание предмета должно содержать от 1 до 1000 символов")));
    }

    @Test
    void create_whenAvailableNull_thenReturnErrorAndStatusBadRequest() throws Exception {
        ItemRequestIdDto itemRequestIdDto = ItemRequestIdDto.builder()
                .id(1L)
                .name("Test name")
                .description("Test description")
                .available(null)
                .requestId(1L)
                .build();

        String json = mapper.writeValueAsString(itemRequestIdDto);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        containsString("create.itemRequestIdDto.available:" +
                                " Статус доступности предмета не может отсутствовать")));
    }

    @Test
    void update_whenItemDtoCorrectAndUserAndItemFound_thenReturnItemDtoAndStatusOk() throws Exception {
        ResponseEntity<Object> response = ResponseEntity.ok().body(itemDto);

        when(itemClient.update(1L, 1L, itemDto)).thenReturn(response);

        String json = mapper.writeValueAsString(itemDto);
        mvc.perform(patch(PATH_VARIABLE_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));
    }

    @Test
    void update_nameWhenUserAndItemFound_thenReturnItemDtoAndStatusOk() throws Exception {
        ItemDto itemDtoWithUpdatedField = ItemDto.builder().name("test2").build();

        itemDto = ItemDto.builder()
                .id(1L)
                .name("test2")
                .description("Test description")
                .available(true)
                .build();

        ResponseEntity<Object> response = ResponseEntity.ok().body(itemDto);

        when(itemClient.update(1L, 1L, itemDtoWithUpdatedField)).thenReturn(response);

        String json = mapper.writeValueAsString(itemDtoWithUpdatedField);
        mvc.perform(patch(PATH_VARIABLE_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));
    }

    @Test
    void update_descriptionWhenUserAndItemFound_thenReturnItemDtoAndStatusOk() throws Exception {
        ItemDto itemDtoWithUpdatedField = ItemDto.builder().description("Test description2").build();

        itemDto = ItemDto.builder()
                .id(1L)
                .name("test")
                .description("Test description2")
                .available(true)
                .build();

        ResponseEntity<Object> response = ResponseEntity.ok().body(itemDto);

        when(itemClient.update(1L, 1L, itemDtoWithUpdatedField)).thenReturn(response);

        String json = mapper.writeValueAsString(itemDtoWithUpdatedField);
        mvc.perform(patch(PATH_VARIABLE_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));
    }

    @Test
    void update_availableWhenUserAndItemFound_thenReturnItemDtoAndStatusOk() throws Exception {
        ItemDto itemDtoWithUpdatedField = ItemDto.builder().available(false).build();

        itemDto = ItemDto.builder()
                .id(1L)
                .name("test")
                .description("Test description")
                .available(false)
                .build();

        ResponseEntity<Object> response = ResponseEntity.ok().body(itemDto);

        when(itemClient.update(1L, 1L, itemDtoWithUpdatedField)).thenReturn(response);

        String json = mapper.writeValueAsString(itemDtoWithUpdatedField);
        mvc.perform(patch(PATH_VARIABLE_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));
    }

    @Test
    void update_whenUserNotFound_thenReturnErrorAndStatusNotFound() throws Exception {
        when(itemClient.update(1L, 1L, itemDto))
                .thenThrow(new NotFoundException(USER_NOT_FOUND_ERROR));

        String json = mapper.writeValueAsString(itemRequestIdDto);
        mvc.perform(patch(PATH_VARIABLE_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(USER_NOT_FOUND_ERROR)));
    }

    @Test
    void update_whenItemNotFound_thenReturnErrorAndStatusNotFound() throws Exception {
        when(itemClient.update(1L, 1L, itemDto))
                .thenThrow(new NotFoundException(ITEM_NOT_FOUND_ERROR));

        String json = mapper.writeValueAsString(itemRequestIdDto);
        mvc.perform(patch(PATH_VARIABLE_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(ITEM_NOT_FOUND_ERROR)));
    }

    @Test
    void update_whenUserNotOwner_thenReturnErrorAndStatusNotFound() throws Exception {
        when(itemClient.update(1L, 1L, itemDto))
                .thenThrow(new NotOwnerException(NOT_OWNER_ERROR));

        String json = mapper.writeValueAsString(itemRequestIdDto);
        mvc.perform(patch(PATH_VARIABLE_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(NOT_OWNER_ERROR)));
    }

    @Test
    void update_whenNameLessThan1Char_thenReturnErrorAndStatusBadRequest() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("")
                .description("Test description")
                .available(true)
                .build();

        String json = mapper.writeValueAsString(itemDto);
        mvc.perform(patch(PATH_VARIABLE_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        containsString("update.itemDto.name:" +
                                " Название предмета должно содержать от 1 до 200 символов")));
    }

    @Test
    void update_whenNameMoreThan200Chars_thenReturnErrorAndStatusBadRequest() throws Exception {
        String badName = new String(new char[201]).replace('\0', 'N');
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name(badName)
                .description("Test description")
                .available(true)
                .build();

        String json = mapper.writeValueAsString(itemDto);
        mvc.perform(patch(PATH_VARIABLE_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        containsString("update.itemDto.name:" +
                                " Название предмета должно содержать от 1 до 200 символов")));
    }

    @Test
    void update_whenDescriptionLessThan1Char_thenReturnErrorAndStatusBadRequest() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Test name")
                .description("")
                .available(true)
                .build();

        String json = mapper.writeValueAsString(itemDto);
        mvc.perform(patch(PATH_VARIABLE_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        containsString("update.itemDto.description:" +
                                " Описание предмета должно содержать от 1 до 1000 символов")));
    }

    @Test
    void update_whenDescriptionMoreThan1000Chars_thenReturnErrorAndStatusBadRequest() throws Exception {
        String badDescription = new String(new char[1001]).replace('\0', 'N');
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Test name")
                .description(badDescription)
                .available(true)
                .build();

        String json = mapper.writeValueAsString(itemDto);
        mvc.perform(patch(PATH_VARIABLE_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        containsString("update.itemDto.description:" +
                                " Описание предмета должно содержать от 1 до 1000 символов")));
    }

    @Test
    void getById_whenItemFound_thenReturnItemAndStatusOk() throws Exception {
        ResponseEntity<Object> response = ResponseEntity.ok().body(itemBookingsAndCommentsDto);

        when(itemClient.getById(1L, 1L)).thenReturn(response);

        mvc.perform(get(PATH_VARIABLE_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemBookingsAndCommentsDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemBookingsAndCommentsDto.getDescription())))
                .andExpect(jsonPath("$.name", is(itemBookingsAndCommentsDto.getName())))
                .andExpect(jsonPath("$.available", is(itemBookingsAndCommentsDto.getAvailable())))

                .andExpect(jsonPath("$.lastBooking.id", is(lastBooking.getId()), Long.class))
                .andExpect(jsonPath("$.lastBooking.bookerId", is(lastBooking.getBookerId()), Long.class))

                .andExpect(jsonPath("$.nextBooking.id", is(nextBooking.getId()), Long.class))
                .andExpect(jsonPath("$.nextBooking.bookerId", is(nextBooking.getBookerId()), Long.class))

                .andExpect(jsonPath("$.comments", hasSize(1)))
                .andExpect(jsonPath("$.comments[0].id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.comments[0].text", is(commentDto.getText())))
                .andExpect(jsonPath("$.comments[0].authorName", is(commentDto.getAuthorName())))
                .andExpect(jsonPath("$.comments[0].created",
                        containsString(String.valueOf(commentDto.getCreated().getSecond()))));
    }

    @Test
    void getById_whenItemNotFound_thenReturnErrorAndStatusBadRequest() throws Exception {
        when(itemClient.getById(1L, 1L)).thenThrow(new NotFoundException(ITEM_NOT_FOUND_ERROR));

        mvc.perform(get(PATH_VARIABLE_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error",
                        containsString(ITEM_NOT_FOUND_ERROR)));
    }

    @Test
    void getAllByOwnerId_whenUserFoundAndItemsExist_thenReturnItemsAndStatusOk() throws Exception {
        ResponseEntity<Object> response = ResponseEntity.ok().body(List.of(itemBookingsDto));

        when(itemClient.getAllByOwnerId(anyLong(), ArgumentMatchers.any(PaginationConfig.class)))
                .thenReturn(response);

        mvc.perform(get(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemBookingsAndCommentsDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemBookingsAndCommentsDto.getDescription())))
                .andExpect(jsonPath("$[0].name", is(itemBookingsAndCommentsDto.getName())))
                .andExpect(jsonPath("$[0].available", is(itemBookingsAndCommentsDto.getAvailable())))

                .andExpect(jsonPath("$[0].lastBooking.id", is(lastBooking.getId()), Long.class))
                .andExpect(jsonPath("$[0].lastBooking.bookerId", is(lastBooking.getBookerId()), Long.class))

                .andExpect(jsonPath("$[0].nextBooking.id", is(nextBooking.getId()), Long.class))
                .andExpect(jsonPath("$[0].nextBooking.bookerId", is(nextBooking.getBookerId()), Long.class));
    }

    @Test
    void getAllByOwnerId_whenUserFoundAndItemsNotExist_thenReturnEmptyListAndStatusOk() throws Exception {
        ResponseEntity<Object> response = ResponseEntity.ok().body(Collections.emptyList());

        when(itemClient.getAllByOwnerId(anyLong(), ArgumentMatchers.any(PaginationConfig.class)))
                .thenReturn(response);

        mvc.perform(get(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(0)));
    }

    @Test
    void getAllByOwnerId_whenUserNotFound_thenThrowErrorAndStatusNotFound() throws Exception {
        when(itemClient.getAllByOwnerId(anyLong(), ArgumentMatchers.any(PaginationConfig.class)))
                .thenThrow(new NotFoundException(USER_NOT_FOUND_ERROR));

        mvc.perform(get(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(USER_NOT_FOUND_ERROR)));
    }

    @Test
    void getAllByOwnerId_whenFromParamLessThan0_thenThrowErrorAndStatusBadRequest() throws Exception {
        String fromParam = "?from=-1";

        mvc.perform(get(URL + fromParam)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("must be greater than or equal to 0")));
    }

    @Test
    void getAllByOwnerId_whenSizeParamLessThan1_thenThrowErrorAndStatusBadRequest() throws Exception {
        String sizeParam = "?size=0";

        mvc.perform(get(URL + sizeParam)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("must be greater than 0")));
    }

    @Test
    void getAllByTextQuery_whenUserFoundAndTextNotEmpty_thenReturnItemsAndStatusOk() throws Exception {
        ResponseEntity<Object> response = ResponseEntity.ok().body(List.of(itemDto));

        when(itemClient.getAllByTextQuery(anyLong(), anyString(), ArgumentMatchers.any(PaginationConfig.class)))
                .thenReturn(response);

        mvc.perform(get(SEARCH_URL + "?text=\"test\"")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].available", is(itemDto.getAvailable())));
    }

    @Test
    void getAllByTextQuery_whenUserFoundAndTextEmpty_thenReturnItemsAndStatusOk() throws Exception {
        ResponseEntity<Object> response = ResponseEntity.ok().body(Collections.emptyList());

        when(itemClient.getAllByTextQuery(anyLong(), ArgumentMatchers.matches(""),
                ArgumentMatchers.any(PaginationConfig.class))).thenReturn(response);

        mvc.perform(get(SEARCH_URL + "?text=\"\"")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(0)));
    }

    @Test
    void getAllByTextQuery_whenUserNotFound_thenThrowErrorAndStatusNotFound() throws Exception {
        when(itemClient.getAllByTextQuery(anyLong(), anyString(),
                ArgumentMatchers.any(PaginationConfig.class))).thenThrow(new NotFoundException(USER_NOT_FOUND_ERROR));

        mvc.perform(get(SEARCH_URL + "?text=\"test\"")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(USER_NOT_FOUND_ERROR)));
    }

    @Test
    void getAllByTextQuery_whenFromParamLessThan0_thenThrowErrorAndStatusBadRequest() throws Exception {
        String params = "?text=\"test\"&from=-1";

        mvc.perform(get(SEARCH_URL + params)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("must be greater than or equal to 0")));
    }

    @Test
    void getAllByTextQuery_whenSizeParamLessThan1_thenThrowErrorAndStatusBadRequest() throws Exception {
        String params = "?text=\"test\"&size=0";

        mvc.perform(get(SEARCH_URL + params)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("must be greater than 0")));
    }

    @Test
    void delete_whenUserAndItemFoundAndUserOwner_thenReturnStatusOk() throws Exception {
        mvc.perform(delete(PATH_VARIABLE_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void delete_whenUserNotFound_thenReturnErrorAndStatusNotFound() throws Exception {
        when(itemClient.deleteItem(1L, 1L)).thenThrow(new NotFoundException(USER_NOT_FOUND_ERROR));

        mvc.perform(delete(PATH_VARIABLE_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(USER_NOT_FOUND_ERROR)));
    }

    @Test
    void delete_whenItemNotFound_thenReturnErrorAndStatusNotFound() throws Exception {
        when(itemClient.deleteItem(1L, 1L)).thenThrow(new NotFoundException(ITEM_NOT_FOUND_ERROR));

        mvc.perform(delete(PATH_VARIABLE_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(ITEM_NOT_FOUND_ERROR)));
    }

    @Test
    void delete_whenUserNotOwner_thenReturnErrorAndStatusNotFound() throws Exception {
        when(itemClient.deleteItem(1L, 1L)).thenThrow(new NotFoundException(NOT_OWNER_ERROR));

        mvc.perform(delete(PATH_VARIABLE_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(NOT_OWNER_ERROR)));
    }

    @Test
    void createComment_whenUserAndItemFoundAndUserBooker_thenReturnCommentDtoAndStatusOk() throws Exception {
        ResponseEntity<Object> response = ResponseEntity.ok().body(commentDto);

        when(itemClient.createComment(1L, 1L, commentTextDto)).thenReturn(response);

        String json = mapper.writeValueAsString(commentTextDto);
        mvc.perform(post(COMMENT_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())))
                .andExpect(jsonPath("$.created",
                        containsString(String.valueOf(commentDto.getCreated().getSecond()))));
    }

    @Test
    void createComment_whenUserNotFound_thenReturnErrorAndStatusNotFound() throws Exception {
        when(itemClient.createComment(1L, 1L, commentTextDto))
                .thenThrow(new NotFoundException(USER_NOT_FOUND_ERROR));

        String json = mapper.writeValueAsString(commentTextDto);
        mvc.perform(post(COMMENT_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(USER_NOT_FOUND_ERROR)));
    }

    @Test
    void createComment_whenItemNotFound_thenReturnErrorAndStatusNotFound() throws Exception {
        when(itemClient.createComment(1L, 1L, commentTextDto))
                .thenThrow(new NotFoundException(ITEM_NOT_FOUND_ERROR));

        String json = mapper.writeValueAsString(commentTextDto);
        mvc.perform(post(COMMENT_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(ITEM_NOT_FOUND_ERROR)));
    }

    @Test
    void createComment_whenUserNotBooker_thenReturnErrorAndStatusBadRequest() throws Exception {
        when(itemClient.createComment(1L, 1L, commentTextDto))
                .thenThrow(new NotAvailableException("Пользователь с id 1 раньше не бронировал предмет с id 1"));

        String json = mapper.writeValueAsString(commentTextDto);
        mvc.perform(post(COMMENT_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        containsString("Пользователь с id 1 раньше не бронировал предмет с id 1")));
    }
}