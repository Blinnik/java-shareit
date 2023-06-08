package ru.practicum.shareit.user.dao.impl;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Service
public class UserDaoImpl implements UserDao {
    final Map<Long, User> usersById = new HashMap<>();
    Long lastId = 1L;

    @Override
    public User save(User user) {
        user.setId(lastId++);
        usersById.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(Long userId, User user) {
        usersById.put(userId, user);
        return user;
    }

    @Override
    public Optional<User> findOne(Long userId) {
        return Optional.ofNullable(usersById.get(userId));
    }

    @Override
    public List<User> findMany() {
        return new ArrayList<>(usersById.values());
    }

    @Override
    public void delete(Long userId) {
        usersById.remove(userId);
    }

    @Override
    public Map<Long, String> getEmailsByUserId() {
        Map<Long, String> emailsByUserId = new HashMap<>();
        for (User user : usersById.values()) {
            emailsByUserId.put(user.getId(), user.getEmail());
        }
        return emailsByUserId;
    }

    @Override
    public boolean isEmailExists(String email) {
        return getEmailsByUserId().containsValue(email);
    }

    @Override
    public boolean notExists(Long userId) {
        return findOne(userId).isEmpty();
    }
}
