package ru.practicum.shareit.request.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.model.dto.RequestDescriptionDto;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@FieldDefaults(level = AccessLevel.PRIVATE)
class RequestDescriptionDtoJsonTest {
    @Autowired
    JacksonTester<RequestDescriptionDto> jackson;

    final String description = "Test description";

    @Test
    void serializeRequestDescriptionDto() throws IOException {
        RequestDescriptionDto requestDescriptionDto = new RequestDescriptionDto(description);

        JsonContent<RequestDescriptionDto> result = jackson.write(requestDescriptionDto);

        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(description);
    }

    @Test
    void deserializeRequestDescriptionDto() throws IOException {
        String json = String.format("{\"description\": \"%s\"}", description);

        RequestDescriptionDto result = this.jackson.parse(json).getObject();

        assertThat(result.getDescription()).isEqualTo(description);
    }
}