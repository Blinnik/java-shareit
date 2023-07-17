package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.dto.UserDto;

import java.util.List;

public interface UserService {
    User create(UserDto userDto);

    User update(Long userId, UserDto userDto);

    User getById(Long userId);

    List<User> getAll();

    void delete(Long userId);
}
