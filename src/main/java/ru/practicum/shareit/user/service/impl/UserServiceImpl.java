package ru.practicum.shareit.user.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    UserDao userDao;

    @Override
    public User create(User user) {
        if (userDao.isEmailExists(user.getEmail())) {
            throw new AlreadyExistsException("Указанный email уже существует");
        }

        User createdUser = userDao.save(user);
        log.info("Был добавлен новый пользователь, id={}", createdUser.getId());

        return createdUser;
    }

    @Override
    public User update(Long userId, User user) {
        if (userDao.notExists(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        user.setId(userId); // Возможно, это лучше отнести на уровень ниже, в UserDao

        String email = user.getEmail();
        if (!userDao.getEmailsByUserId().get(userId).equals(email) &&
                userDao.isEmailExists(email)) {
            throw new AlreadyExistsException("Email " + email + " уже существует");
        }

        User foundUser = getOne(userId);
        if (user.getName() == null) {
            user.setName(foundUser.getName());
        }
        if (user.getEmail() == null) {
            user.setEmail(foundUser.getEmail());
        }

        User updatedUser = userDao.update(userId, user);
        log.info("Пользователь с id {} был обновлен", userId);

        return updatedUser;
    }

    @Override
    public User getOne(Long userId) {
        User user = userDao.findOne(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        log.info("Получен пользователь с id {}: {}", userId, user);

        return user;
    }

    @Override
    public List<User> getMany() {
        List<User> users = userDao.findMany();
        log.info("Получен список всех пользователей");

        return users;
    }

    @Override
    public void delete(Long userId) {
        if (userDao.notExists(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        userDao.delete(userId);
        log.info("Пользователь с id {} был удален", userId);
    }
}
