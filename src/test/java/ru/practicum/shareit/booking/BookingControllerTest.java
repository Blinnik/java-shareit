package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.dto.BookingDto;
import ru.practicum.shareit.booking.model.dto.BookingItemIdAndTimeDto;
import ru.practicum.shareit.booking.model.dto.BookingStatusDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.common.exception.NotAvailableException;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.common.exception.NotOwnerException;
import ru.practicum.shareit.common.model.PaginationConfig;
import ru.practicum.shareit.user.model.dto.ItemBookerDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class BookingControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    BookingService bookingService;

    @Autowired
    MockMvc mvc;

    static final String URL = "/bookings";
    static final String PATH_VARIABLE_URL = "/bookings/1";
    static final String OWNER_URL = "/bookings/owner";
    static final String USER_NOT_FOUND_ERROR = "Пользователь с id 1 не найден";
    static final String ITEM_NOT_FOUND_ERROR = "Предмет с id 1 не найден";
    static final String BOOKING_NOT_FOUND_ERROR = "Бронь с id 1 не найдена";
    static final String NOT_OWNER_ERROR = "Пользователь с id 1 не является владельцем предмета с id 1";
    final ItemBookerDto booker = ItemBookerDto.builder()
            .id(1L)
            .name("test user")
            .build();
    BookingDto bookingDto;
    BookingItemIdAndTimeDto bookingItemIdAndTimeDto;
    LocalDateTime start;
    LocalDateTime end;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        start = now.plusDays(1);
        end = now.plusDays(10);

        bookingDto = BookingDto.builder()
                .id(1L)
                .start(start)
                .end(end)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        bookingItemIdAndTimeDto = new BookingItemIdAndTimeDto(1L, start.toString(), end.toString());
    }

    @Test
    void create_whenItemFoundAndAvailableAndUserFoundAndNotOwner_thenReturnBookingDtoAndStatusOk() throws Exception {
        when(bookingService.create(1L, bookingItemIdAndTimeDto)).thenReturn(bookingDto);

        String json = mapper.writeValueAsString(bookingItemIdAndTimeDto);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start",
                        containsString(String.valueOf(bookingDto.getStart().getSecond()))))
                .andExpect(jsonPath("$.end",
                        containsString(String.valueOf(bookingDto.getEnd().getSecond()))))
                .andExpect(jsonPath("$.booker.id", is(bookingDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(bookingDto.getBooker().getName())))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())));
    }

    @Test
    void create_whenItemIdNull_thenReturnErrorAndStatusBadRequest() throws Exception {
        BookingItemIdAndTimeDto bookingItemIdAndTimeDto =
                new BookingItemIdAndTimeDto(null, start.toString(), end.toString());

        String json = mapper.writeValueAsString(bookingItemIdAndTimeDto);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("must not be null")));
    }

    @Test
    void create_whenStartNull_thenReturnErrorAndStatusBadRequest() throws Exception {
        BookingItemIdAndTimeDto bookingItemIdAndTimeDto =
                new BookingItemIdAndTimeDto(1L, null, end.toString());

        String json = mapper.writeValueAsString(bookingItemIdAndTimeDto);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("must not be null")));
    }

    @Test
    void create_whenEndNull_thenReturnErrorAndStatusBadRequest() throws Exception {
        BookingItemIdAndTimeDto bookingItemIdAndTimeDto =
                new BookingItemIdAndTimeDto(1L, start.toString(), null);

        String json = mapper.writeValueAsString(bookingItemIdAndTimeDto);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("must not be null")));
    }

    @Test
    void create_whenItemNotFound_thenReturnErrorAndStatusNotFound() throws Exception {
        when(bookingService.create(1L, bookingItemIdAndTimeDto))
                .thenThrow(new NotFoundException(ITEM_NOT_FOUND_ERROR));

        String json = mapper.writeValueAsString(bookingItemIdAndTimeDto);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(ITEM_NOT_FOUND_ERROR)));
    }

    @Test
    void create_whenItemNotAvailable_thenReturnErrorAndStatusBadRequest() throws Exception {
        when(bookingService.create(1L, bookingItemIdAndTimeDto))
                .thenThrow(new NotAvailableException("Предмет не доступен для брони"));

        String json = mapper.writeValueAsString(bookingItemIdAndTimeDto);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Предмет не доступен для брони")));
    }

    @Test
    void create_whenUserNotFound_thenReturnErrorAndStatusNotFound() throws Exception {
        when(bookingService.create(1L, bookingItemIdAndTimeDto))
                .thenThrow(new NotFoundException(USER_NOT_FOUND_ERROR));

        String json = mapper.writeValueAsString(bookingItemIdAndTimeDto);
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
    void create_whenUserOwner_thenReturnErrorAndStatusNotFound() throws Exception {
        when(bookingService.create(1L, bookingItemIdAndTimeDto))
                .thenThrow(new NotFoundException("Пользователь не может забронировать собственный предмет"));

        String json = mapper.writeValueAsString(bookingItemIdAndTimeDto);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error",
                        containsString("Пользователь не может забронировать собственный предмет")));
    }

    @Test
    void create_whenStartAfterEnd_thenReturnErrorAndStatusBadRequest() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.minusSeconds(1);

        BookingItemIdAndTimeDto bookingItemIdAndTimeDto =
                new BookingItemIdAndTimeDto(1L, start.toString(), end.toString());

        String json = mapper.writeValueAsString(bookingItemIdAndTimeDto);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        containsString("Время конца брони не может быть раньше времени начала")));
    }

    @Test
    void create_whenStartInPast_thenReturnErrorAndStatusBadRequest() throws Exception {
        LocalDateTime start = LocalDateTime.now().minusSeconds(1);
        LocalDateTime end = start.plusSeconds(1);

        BookingItemIdAndTimeDto bookingItemIdAndTimeDto =
                new BookingItemIdAndTimeDto(1L, start.toString(), end.toString());

        String json = mapper.writeValueAsString(bookingItemIdAndTimeDto);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        containsString("Время начала брони не может быть в прошлом")));
    }

    @Test
    void create_whenStartEqualsEnd_thenReturnErrorAndStatusBadRequest() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusSeconds(1);

        BookingItemIdAndTimeDto bookingItemIdAndTimeDto =
                new BookingItemIdAndTimeDto(1L, start.toString(), start.toString());

        String json = mapper.writeValueAsString(bookingItemIdAndTimeDto);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        containsString("Время конца брони не может совпадать с временем начала")));
    }

    @Test
    void updateStatus_whenBookingAndItemFoundAndUserOwnerAndStatusNotSame_thenReturnBookingDtoAndStatusOk()
            throws Exception {
        String approvedParam = "?approved=false";

        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .start(start)
                .end(end)
                .booker(booker)
                .status(BookingStatus.REJECTED)
                .build();

        when(bookingService.updateStatus(1L, 1L, false)).thenReturn(bookingDto);

        String json = mapper.writeValueAsString(bookingItemIdAndTimeDto);
        mvc.perform(patch(PATH_VARIABLE_URL + approvedParam)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start",
                        containsString(String.valueOf(bookingDto.getStart().getSecond()))))
                .andExpect(jsonPath("$.end",
                        containsString(String.valueOf(bookingDto.getEnd().getSecond()))))
                .andExpect(jsonPath("$.booker.id", is(bookingDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(bookingDto.getBooker().getName())))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())));
    }

    @Test
    void updateStatus_whenBookingNotFound_thenReturnErrorAndStatusNotFound() throws Exception {
        String approvedParam = "?approved=false";

        when(bookingService.updateStatus(1L, 1L, false))
                .thenThrow(new NotFoundException(BOOKING_NOT_FOUND_ERROR));

        String json = mapper.writeValueAsString(bookingItemIdAndTimeDto);
        mvc.perform(patch(PATH_VARIABLE_URL + approvedParam)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(BOOKING_NOT_FOUND_ERROR)));
    }

    @Test
    void updateStatus_whenItemNotFound_thenReturnErrorAndStatusNotFound() throws Exception {
        String approvedParam = "?approved=false";

        when(bookingService.updateStatus(1L, 1L, false))
                .thenThrow(new NotFoundException(ITEM_NOT_FOUND_ERROR));

        String json = mapper.writeValueAsString(bookingItemIdAndTimeDto);
        mvc.perform(patch(PATH_VARIABLE_URL + approvedParam)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(ITEM_NOT_FOUND_ERROR)));
    }

    @Test
    void updateStatus_whenUserNotOwner_thenReturnErrorAndStatusNotFound() throws Exception {
        String approvedParam = "?approved=false";

        when(bookingService.updateStatus(1L, 1L, false))
                .thenThrow(new NotOwnerException(NOT_OWNER_ERROR));

        String json = mapper.writeValueAsString(bookingItemIdAndTimeDto);
        mvc.perform(patch(PATH_VARIABLE_URL + approvedParam)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(NOT_OWNER_ERROR)));
    }

    @Test
    void updateStatus_whenStatusSame_thenReturnErrorAndStatusBadRequest() throws Exception {
        String approvedParam = "?approved=true";

        when(bookingService.updateStatus(1L, 1L, true))
                .thenThrow(new NotAvailableException("Нельзя повторно одобрить бронь"));

        String json = mapper.writeValueAsString(bookingItemIdAndTimeDto);
        mvc.perform(patch(PATH_VARIABLE_URL + approvedParam)
                        .header("X-Sharer-User-Id", 1L)
                        .content(json)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Нельзя повторно одобрить бронь")));
    }

    @Test
    void getById_whenBookingFoundAndUserConnectedWithBooking_thenReturnBookingDtoAndStatusOk() throws Exception {
        when(bookingService.getById(1L, 1L)).thenReturn(bookingDto);

        mvc.perform(get(PATH_VARIABLE_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start",
                        containsString(String.valueOf(bookingDto.getStart().getSecond()))))
                .andExpect(jsonPath("$.end",
                        containsString(String.valueOf(bookingDto.getEnd().getSecond()))))
                .andExpect(jsonPath("$.booker.id", is(bookingDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(bookingDto.getBooker().getName())))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())));
    }

    @Test
    void getById_whenBookingNotFound_thenReturnErrorAndStatusNotFound() throws Exception {
        when(bookingService.getById(1L, 1L)).thenThrow(new NotFoundException(BOOKING_NOT_FOUND_ERROR));

        mvc.perform(get(PATH_VARIABLE_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(BOOKING_NOT_FOUND_ERROR)));
    }

    @Test
    void getById_whenUserNotConnectedWithBooking_thenReturnErrorAndStatusNotFound() throws Exception {
        when(bookingService.getById(1L, 1L))
                .thenThrow(new NotFoundException("Нет броней, связанных с пользователем с id 1"));

        mvc.perform(get(PATH_VARIABLE_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error",
                        containsString("Нет броней, связанных с пользователем с id 1")));
    }

    @Test
    void getAllByBookerId_whenBookingsFound_thenReturnBookingsDtoAndStatusOk() throws Exception {
        when(bookingService.getAllByBookerId(anyLong(), any(BookingStatusDto.class), any(PaginationConfig.class)))
                .thenReturn(List.of(bookingDto));

        mvc.perform(get(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].start",
                        containsString(String.valueOf(bookingDto.getStart().getSecond()))))
                .andExpect(jsonPath("$[0].end",
                        containsString(String.valueOf(bookingDto.getEnd().getSecond()))))
                .andExpect(jsonPath("$[0].booker.id", is(bookingDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.name", is(bookingDto.getBooker().getName())))
                .andExpect(jsonPath("$[0].status", is(bookingDto.getStatus().toString())));
    }

    @Test
    void getAllByBookerId_whenBookingsNotFound_thenReturnErrorAndStatusNotFound() throws Exception {
        when(bookingService.getAllByBookerId(anyLong(), any(BookingStatusDto.class), any(PaginationConfig.class)))
                .thenThrow(new NotFoundException("По характеристике WAITING " +
                        "не было найдено вещей, забронированных пользователем с id 1"));

        mvc.perform(get(URL)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("По характеристике WAITING " +
                        "не было найдено вещей, забронированных пользователем с id 1")));
    }

    @Test
    void getAllByOwnerId_whenBookingsFound_thenReturnBookingsDtoAndStatusOk() throws Exception {
        when(bookingService.getAllByOwnerId(anyLong(), any(BookingStatusDto.class), any(PaginationConfig.class)))
                .thenReturn(List.of(bookingDto));

        mvc.perform(get(OWNER_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].start",
                        containsString(String.valueOf(bookingDto.getStart().getSecond()))))
                .andExpect(jsonPath("$[0].end",
                        containsString(String.valueOf(bookingDto.getEnd().getSecond()))))
                .andExpect(jsonPath("$[0].booker.id", is(bookingDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.name", is(bookingDto.getBooker().getName())))
                .andExpect(jsonPath("$[0].status", is(bookingDto.getStatus().toString())));
    }

    @Test
    void getAllByOwnerId_whenBookingsNotFound_thenReturnErrorAndStatusNotFound() throws Exception {
        when(bookingService.getAllByOwnerId(anyLong(), any(BookingStatusDto.class), any(PaginationConfig.class)))
                .thenThrow(new NotFoundException("По характеристике WAITING " +
                        "не было найдено вещей, забронированных у пользователя с id 1"));

        mvc.perform(get(OWNER_URL)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("По характеристике WAITING " +
                        "не было найдено вещей, забронированных у пользователя с id 1")));
    }
}