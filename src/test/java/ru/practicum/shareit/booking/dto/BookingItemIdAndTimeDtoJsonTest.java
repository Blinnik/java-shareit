package ru.practicum.shareit.booking.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.model.dto.BookingItemIdAndTimeDto;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@FieldDefaults(level = AccessLevel.PRIVATE)
class BookingItemIdAndTimeDtoJsonTest {

    @Autowired
    JacksonTester<BookingItemIdAndTimeDto> jackson;

    final Long itemId = 1L;
    final String start = "2010-11-29T12:12:12";
    final String end = "2010-12-29T12:12:12";

    @Test
    void serializeBookingItemIdAndTimeDto() throws IOException {
        BookingItemIdAndTimeDto bookingItemIdAndTimeDto = new BookingItemIdAndTimeDto(itemId, start, end);

        JsonContent<BookingItemIdAndTimeDto> result = jackson.write(bookingItemIdAndTimeDto);

        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(itemId.intValue());
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo(start);
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo(end);
    }

    @Test
    void deserializeBookingItemIdAndTimeDto() throws IOException {
        String json = String.format("{\"itemId\": \"%d\", \"start\": \"%s\", \"end\": \"%s\"}", itemId, start, end);

        BookingItemIdAndTimeDto result = this.jackson.parse(json).getObject();

        assertThat(result.getItemId()).isEqualTo(itemId);
        assertThat(result.getStart()).isEqualTo(start);
        assertThat(result.getEnd()).isEqualTo(end);
    }
}