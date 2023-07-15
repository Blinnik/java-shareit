package ru.practicum.shareit.request.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.model.dto.RequestDto;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@FieldDefaults(level = AccessLevel.PRIVATE)
class RequestDtoJsonTest {
    @Autowired
    JacksonTester<RequestDto> jackson;

    final Long id = 1L;
    final String description = "Test description";
    final LocalDateTime created = LocalDateTime.now();

    @Test
    void serializeRequestDto() throws IOException {
        RequestDto requestDescriptionDto = RequestDto.builder()
                .id(id)
                .description(description)
                .created(created)
                .build();

        JsonContent<RequestDto> result = jackson.write(requestDescriptionDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(id.intValue());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(description);
        assertThat(result).extractingJsonPathStringValue("$.created")
                .contains(String.valueOf(created.getSecond()));
    }

    @Test
    void deserializeRequestDto() throws IOException {
        String json = String.format("{\"id\": \"%d\", \"description\": \"%s\", " +
                "\"created\": \"%s\"}", id, description, created);

        RequestDto result = this.jackson.parse(json).getObject();

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getDescription()).isEqualTo(description);
        assertThat(result.getCreated()).isEqualTo(created);
    }
}