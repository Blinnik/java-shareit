package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.model.dto.ItemRequestIdDto;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemRequestIdDtoJsonTest {
    @Autowired
    JacksonTester<ItemRequestIdDto> jackson;

    final Long id = 1L;
    final String name = "Test name";
    final String description = "Test description";
    final boolean available = false;
    final Long requestId = 1L;

    @Test
    void serializeItemRequestIdDto() throws IOException {
        ItemRequestIdDto itemRequestIdDto = ItemRequestIdDto.builder()
                .id(id)
                .name(name)
                .description(description)
                .available(available)
                .requestId(requestId)
                .build();

        JsonContent<ItemRequestIdDto> result = jackson.write(itemRequestIdDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(id.intValue());
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(name);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(description);
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(available);
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(requestId.intValue());
    }

    @Test
    void deserializeItemRequestIdDto() throws IOException {
        String json = String.format("{\"id\": \"%d\", \"name\": \"%s\", \"description\": \"%s\", " +
                "\"available\": \"%b\", \"requestId\": \"%d\"}", id, name, description, available, requestId);

        ItemRequestIdDto result = this.jackson.parse(json).getObject();

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getDescription()).isEqualTo(description);
        assertThat(result.getAvailable()).isEqualTo(available);
        assertThat(result.getRequestId()).isEqualTo(requestId);
    }
}