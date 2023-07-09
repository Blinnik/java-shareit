package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.model.dto.CommentTextDto;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@FieldDefaults(level = AccessLevel.PRIVATE)
class CommentTextDtoJsonTest {
    @Autowired
    JacksonTester<CommentTextDto> jackson;

    final String text = "Test text";

    @Test
    void serializeCommentTextDto() throws IOException {
        CommentTextDto commentTextDto = new CommentTextDto(text);

        JsonContent<CommentTextDto> result = jackson.write(commentTextDto);

        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo(text);
    }

    @Test
    void deserializeCommentTextDto() throws IOException {
        String json = String.format("{\"text\": \"%s\"}", text);

        CommentTextDto result = this.jackson.parse(json).getObject();

        assertThat(result.getText()).isEqualTo(text);
    }
}