package ru.practicum.shareit.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.common.marker.ValidationMarker;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequiredArgsConstructor
@Validated
public class UserController {
    UserClient userClient;

    @PostMapping
    @Validated(ValidationMarker.OnCreate.class)
    public ResponseEntity<Object> create(@RequestBody @Valid UserDto userDto) {
        log.info("Создаем user {}", userDto);
        return userClient.create(userDto);
    }

    @PatchMapping("/{userId}")
    @Validated(ValidationMarker.OnUpdate.class)
    public ResponseEntity<Object> update(@PathVariable Long userId,
                                         @RequestBody @Valid UserDto userDto) {
        log.info("Обновляем user {}, userId={}", userDto, userId);
        return userClient.update(userId, userDto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getById(@PathVariable Long userId) {
        log.info("Получаем user, userId={}", userId);
        return userClient.getById(userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        log.info("Получаем всех user");
        return userClient.getAll();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> delete(@PathVariable Long userId) {
        log.info("Удаляем user, userId={}", userId);
        return userClient.deleteUser(userId);
    }
}
