package ru.practicum.shareit.user.dao;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserDao {
    User save(User user);

    User update(Long userId, User user);

    Optional<User> findOne(Long userId);

    List<User> findMany();

    void delete(Long userId);

    Map<Long, String> getEmailsByUserId();

    boolean isEmailExists(String email);

    boolean notExists(Long userId);
}
