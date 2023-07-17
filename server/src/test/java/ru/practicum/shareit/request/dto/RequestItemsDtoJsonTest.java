package ru.practicum.shareit.request.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.model.dto.ItemRequestIdDto;
import ru.practicum.shareit.request.model.dto.RequestItemsDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@FieldDefaults(level = AccessLevel.PRIVATE)
class RequestItemsDtoJsonTest {
    @Autowired
    JacksonTester<RequestItemsDto> jackson;

    final Long id = 1L;
    final String description = "Test description";
    final LocalDateTime created = LocalDateTime.now();
    final List<ItemRequestIdDto> itemRequestIdDtos = List.of();

    @Test
    void serializeRequestDto() throws IOException {
        RequestItemsDto requestItemsDto = RequestItemsDto.builder()
                .id(id)
                .description(description)
                .created(created)
                .items(itemRequestIdDtos)
                .build();

        JsonContent<RequestItemsDto> result = jackson.write(requestItemsDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(id.intValue());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(description);
        assertThat(result).extractingJsonPathStringValue("$.created")
                .contains(String.valueOf(created.getSecond()));
        assertThat(result).extractingJsonPathArrayValue("$.items").isEqualTo(itemRequestIdDtos);
    }

    @Test
    void deserializeRequestDto() throws IOException {
        String json = String.format("{\"id\": \"%d\", \"description\": \"%s\", " +
                "\"created\": \"%s\", \"items\": []}", id, description, created);

        RequestItemsDto result = this.jackson.parse(json).getObject();

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getDescription()).isEqualTo(description);
        assertThat(result.getCreated()).isEqualTo(created);
        assertThat(result.getItems()).isEqualTo(itemRequestIdDtos);
    }
}