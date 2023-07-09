package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.model.dto.CommentDto;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@FieldDefaults(level = AccessLevel.PRIVATE)
class CommentDtoJsonTest {
    @Autowired
    JacksonTester<CommentDto> jackson;

    final Long id = 1L;
    final String text = "Test text";
    final String authorName = "Test author";
    final LocalDateTime created = LocalDateTime.now();

    @Test
    void serializeRequestDto() throws IOException {
        CommentDto commentDto = new CommentDto(id, text, authorName, created);

        JsonContent<CommentDto> result = jackson.write(commentDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(id.intValue());
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo(text);
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo(authorName);
        assertThat(result).extractingJsonPathStringValue("$.created")
                .contains(String.valueOf(created.getSecond()));
    }

    @Test
    void deserializeRequestDto() throws IOException {
        String json = String.format("{\"id\": \"%d\", \"text\": \"%s\", " +
                "\"authorName\": \"%s\", \"created\": \"%s\"}", id, text, authorName, created);

        CommentDto result = this.jackson.parse(json).getObject();

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getText()).isEqualTo(text);
        assertThat(result.getAuthorName()).isEqualTo(authorName);
        assertThat(result.getCreated()).isEqualTo(created);
    }
}
