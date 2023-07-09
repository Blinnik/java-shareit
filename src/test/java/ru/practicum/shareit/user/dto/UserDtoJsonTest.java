package ru.practicum.shareit.user.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.model.dto.UserDto;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@FieldDefaults(level = AccessLevel.PRIVATE)
class UserDtoJsonTest {
    @Autowired
    JacksonTester<UserDto> jackson;

    final String name = "name";
    final String email = "name@mail.com";

    @Test
    void serializeUserDto() throws IOException {
        UserDto userDto = UserDto.builder()
                .name(name)
                .email(email)
                .build();

        JsonContent<UserDto> result = jackson.write(userDto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(name);
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo(email);
    }

    @Test
    void deserializeUserDto() throws IOException {
        String json = String.format("{\"name\": \"%s\", \"email\": \"%s\"}", name, email);

        UserDto result = this.jackson.parse(json).getObject();

        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getEmail()).isEqualTo(email);
    }
}