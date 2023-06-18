package ru.practicum.shareit.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.marker.ValidationMarker;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Validated
public class UserController {
    UserService userService;

    @PostMapping
    @Validated(ValidationMarker.OnCreate.class)
    public User create(@RequestBody @Valid UserDto userDto) {
        return userService.create(UserMapper.toUser(userDto));
    }

    @PatchMapping("/{userId}")
    @Validated(ValidationMarker.OnUpdate.class)
    public User update(@PathVariable Long userId,
                       @RequestBody @Valid UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        user.setId(userId);
        return userService.update(userId, user);
    }

    @GetMapping("/{userId}")
    public User getOne(@PathVariable Long userId) {
        return userService.getOne(userId);
    }

    @GetMapping
    public List<User> getMany() {
        return userService.getMany();
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        userService.delete(userId);
    }
}
