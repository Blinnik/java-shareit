package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.model.dto.ItemDto;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemDtoJsonTest {
    @Autowired
    JacksonTester<ItemDto> jackson;

    final Long id = 1L;
    final String name = "Test name";
    final String description = "Test description";
    final boolean available = false;

    @Test
    void serializeItemDto() throws IOException {
        ItemDto itemDto = ItemDto.builder()
                .id(id)
                .name(name)
                .description(description)
                .available(available)
                .build();

        JsonContent<ItemDto> result = jackson.write(itemDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(id.intValue());
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(name);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(description);
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(available);
    }

    @Test
    void deserializeItemDto() throws IOException {
        String json = String.format("{\"id\": \"%d\", \"name\": \"%s\", \"description\": \"%s\", " +
                "\"available\": \"%b\"}", id, name, description, available);

        ItemDto result = this.jackson.parse(json).getObject();

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getDescription()).isEqualTo(description);
        assertThat(result.getAvailable()).isEqualTo(available);
    }
}