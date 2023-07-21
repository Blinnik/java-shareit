package ru.practicum.shareit.booking.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.dto.BookingDto;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.user.model.dto.ItemBookerDto;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@FieldDefaults(level = AccessLevel.PRIVATE)
class BookingDtoJsonTest {
    @Autowired
    JacksonTester<BookingDto> jackson;

    final Long id = 1L;
    final LocalDateTime start = LocalDateTime.now().plusDays(1);
    final LocalDateTime end = LocalDateTime.now().plusDays(10);
    final ItemDto item = ItemDto.builder()
            .id(1L)
            .name("test name")
            .description("test description")
            .available(true)
            .build();
    final ItemBookerDto booker = ItemBookerDto.builder()
            .id(1L)
            .name("booker name")
            .build();
    final BookingStatus bookingStatus = BookingStatus.WAITING;

    @Test
    void serializeRequestDto() throws IOException {
        BookingDto bookingDto = BookingDto.builder()
                .id(id)
                .status(bookingStatus)
                .booker(booker)
                .item(item)
                .start(start)
                .end(end)
                .build();

        JsonContent<BookingDto> result = jackson.write(bookingDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(id.intValue());
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo(bookingStatus.toString());
        assertThat(result).extractingJsonPathStringValue("$.start").contains(String.valueOf(start.getSecond()));
        assertThat(result).extractingJsonPathStringValue("$.end").contains(String.valueOf(end.getSecond()));

        assertThat(result).extractingJsonPathValue("$.item").extracting("id")
                .isEqualTo(item.getId().intValue());
        assertThat(result).extractingJsonPathValue("$.item").extracting("name")
                .isEqualTo(item.getName());
        assertThat(result).extractingJsonPathValue("$.item").extracting("description")
                .isEqualTo(item.getDescription());
        assertThat(result).extractingJsonPathValue("$.item").extracting("available")
                .isEqualTo(item.getAvailable());

        assertThat(result).extractingJsonPathValue("$.booker").extracting("id")
                .isEqualTo(booker.getId().intValue());
        assertThat(result).extractingJsonPathValue("$.booker").extracting("name")
                .isEqualTo(booker.getName());
    }

    @Test
    void deserializeRequestDto() throws IOException {
        String json = String.format("{\"id\": \"%d\", \"start\": \"%s\", \"end\": \"%s\", " +
                        "\"item\": {\"id\": \"%d\", \"name\": \"%s\", \"description\": \"%s\", \"available\": \"%s\"}, " +
                        "\"booker\": {\"id\": \"%d\", \"name\": \"%s\"}, \"status\": \"%s\"}",
                id, start, end, item.getId(), item.getName(), item.getDescription(), item.getAvailable(),
                booker.getId(), booker.getName(), bookingStatus);

        BookingDto result = this.jackson.parse(json).getObject();

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getStatus()).isEqualTo(bookingStatus);
        assertThat(result.getStart()).isEqualTo(start);
        assertThat(result.getEnd()).isEqualTo(end);
        assertThat(result.getItem()).isEqualTo(item);
        assertThat(result.getBooker()).isEqualTo(booker);
    }
}