package ru.practicum.shareit.request;

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
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.common.model.PaginationConfig;
import ru.practicum.shareit.item.model.dto.ItemRequestIdDto;
import ru.practicum.shareit.request.model.dto.RequestDescriptionDto;
import ru.practicum.shareit.request.model.dto.RequestDto;
import ru.practicum.shareit.request.model.dto.RequestItemsDto;
import ru.practicum.shareit.request.service.RequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RequestController.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class RequestControllerTest {
    @Autowired
    ObjectMapper mapper;

    @MockBean
    RequestService requestService;

    @Autowired
    MockMvc mvc;

    static final String URL = "/requests";
    static final String PATH_VARIABLE_URL = "/requests/1";
    static final String ALL_REQUESTS_URL = "/requests/all";
    static final String USER_NOT_FOUND_ERROR = "Пользователь с id 1 не найден";
    static final String REQUEST_NOT_FOUND_ERROR = "Запрос с id 1 не найден";
    RequestDescriptionDto requestDescriptionDto;
    RequestDto requestDto;
    RequestItemsDto requestItemsDto;

    @BeforeEach
    void setUp() {
        requestDescriptionDto = new RequestDescriptionDto("Test description");

        requestDto = RequestDto.builder()
                .id(1L)
                .description("Test description")
                .created(LocalDateTime.now())
                .build();

        requestItemsDto = RequestItemsDto.builder()
                .id(1L)
                .description("Test description")
                .created(LocalDateTime.now())
                .items(List.of(ItemRequestIdDto.builder().build(), ItemRequestIdDto.builder().build()))
                .build();
    }

    @Test
    void create_whenItemRequestIdDtoCorrectAndUserFound_thenReturnRequestDtoAndStatusOk() throws Exception {
        when(requestService.create(1L, requestDescriptionDto)).thenReturn(requestDto);

        String json = mapper.writeValueAsString(requestDescriptionDto);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(requestDto.getDescription())))
                .andExpect(jsonPath("$.created",
                        containsString(String.valueOf(requestDto.getCreated().getSecond()))));
    }

    @Test
    void create_whenUserNotFound_thenReturnErrorAndStatusNotFound() throws Exception {
        when(requestService.create(1L, requestDescriptionDto))
                .thenThrow(new NotFoundException(USER_NOT_FOUND_ERROR));

        String json = mapper.writeValueAsString(requestDescriptionDto);
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
    void create_whenDescriptionLessThan10Chars_thenReturnErrorAndStatusBadRequest() throws Exception {
        RequestDescriptionDto request = new RequestDescriptionDto("999999999");

        String json = mapper.writeValueAsString(request);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        containsString("Описание запроса на предмет должно содержать от 10 до 500 символов")));
    }

    @Test
    void create_whenDescriptionMoreThan10Chars_thenReturnErrorAndStatusBadRequest() throws Exception {
        String description = new String(new char[501]).replace('\0', 'N');
        RequestDescriptionDto request = new RequestDescriptionDto(description);

        String json = mapper.writeValueAsString(request);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        containsString("Описание запроса на предмет должно содержать от 10 до 500 символов")));
    }

    @Test
    void create_whenDescriptionNull_thenReturnErrorAndStatusBadRequest() throws Exception {
        RequestDescriptionDto request = new RequestDescriptionDto(null);

        String json = mapper.writeValueAsString(request);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        containsString("must not be null")));
    }

    @Test
    void getOwn_whenUserAndRequestsFound_thenReturnListOfRequestsAndStatusOk() throws Exception {
        when(requestService.getOwn(1L)).thenReturn(List.of(requestItemsDto));

        mvc.perform(get(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(requestItemsDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestItemsDto.getDescription())))
                .andExpect(jsonPath("$[0].created",
                        containsString(String.valueOf(requestItemsDto.getCreated().getSecond()))))
                .andExpect(jsonPath("$[0].items", hasSize(2)));
    }

    @Test
    void getOwn_whenUserFoundAndRequestsNotFound_thenReturnEmptyListAndStatusOk() throws Exception {
        when(requestService.getOwn(1L)).thenReturn(Collections.emptyList());

        mvc.perform(get(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(0)));
    }

    @Test
    void getOwn_whenUserNotFound_thenReturnErrorAndStatusNotFound() throws Exception {
        when(requestService.getOwn(1L)).thenThrow(new NotFoundException(USER_NOT_FOUND_ERROR));

        mvc.perform(get(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(USER_NOT_FOUND_ERROR)));
    }

    @Test
    void getAll_whenUserAndRequestsFound_thenReturnListOfRequestsAndStatusOk() throws Exception {
        when(requestService.getAll(anyLong(), ArgumentMatchers.any(PaginationConfig.class)))
                .thenReturn(List.of(requestItemsDto));

        mvc.perform(get(ALL_REQUESTS_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$[0].id", is(requestItemsDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestItemsDto.getDescription())))
                .andExpect(jsonPath("$[0].created",
                        containsString(String.valueOf(requestItemsDto.getCreated().getSecond()))))
                .andExpect(jsonPath("$[0].items", hasSize(2)));
    }

    @Test
    void getAll_whenUserFoundAndRequestsNotFound_thenReturnEmptyListAndStatusOk() throws Exception {
        when(requestService.getAll(anyLong(), ArgumentMatchers.any(PaginationConfig.class)))
                .thenReturn(Collections.emptyList());

        mvc.perform(get(ALL_REQUESTS_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(0)));
    }

    @Test
    void getAll_whenUserNotFound_thenReturnErrorAndStatusNotFound() throws Exception {
        when(requestService.getAll(anyLong(), ArgumentMatchers.any(PaginationConfig.class)))
                .thenThrow(new NotFoundException(USER_NOT_FOUND_ERROR));

        mvc.perform(get(ALL_REQUESTS_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(USER_NOT_FOUND_ERROR)));
    }

    @Test
    void getAll_whenFromParamLessThan0_thenReturnErrorAndStatusBadRequest() throws Exception {
        String fromParam = "?from=-1";

        mvc.perform(get(ALL_REQUESTS_URL + fromParam)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("must be greater than or equal to 0")));
    }

    @Test
    void getAll_whenSizeParamLessThan1_thenReturnErrorAndStatusBadRequest() throws Exception {
        String sizeParam = "?size=0";

        mvc.perform(get(ALL_REQUESTS_URL + sizeParam)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("must be greater than 0")));
    }

    @Test
    void getById_whenUserAndRequestFound_thenReturnRequestAndStatusOk() throws Exception {
        when(requestService.getById(anyLong(), anyLong())).thenReturn(requestItemsDto);

        mvc.perform(get(PATH_VARIABLE_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.id", is(requestItemsDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(requestItemsDto.getDescription())))
                .andExpect(jsonPath("$.created",
                        containsString(String.valueOf(requestItemsDto.getCreated().getSecond()))))
                .andExpect(jsonPath("$.items", hasSize(2)));
    }

    @Test
    void getById_whenRequestNotFound_thenReturnErrorAndStatusNotFound() throws Exception {
        when(requestService.getById(anyLong(), anyLong())).thenThrow(new NotFoundException(REQUEST_NOT_FOUND_ERROR));

        mvc.perform(get(PATH_VARIABLE_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(REQUEST_NOT_FOUND_ERROR)));
    }

    @Test
    void getById_whenUserNotFound_thenReturnErrorAndStatusNotFound() throws Exception {
        when(requestService.getById(anyLong(), anyLong())).thenThrow(new NotFoundException(USER_NOT_FOUND_ERROR));

        mvc.perform(get(PATH_VARIABLE_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(USER_NOT_FOUND_ERROR)));
    }
}