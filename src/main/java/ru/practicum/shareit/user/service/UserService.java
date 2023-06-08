package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    User create(User user);

    User update(Long userId, User user);

    User getOne(Long userId);

    List<User> getMany();

    void delete(Long userId);
}
