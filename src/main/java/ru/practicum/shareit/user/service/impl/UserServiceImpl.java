package ru.practicum.shareit.user.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    UserRepository userRepository;

    @Transactional
    @Override
    public User create(UserDto userDto) {
        User createdUser = userRepository.save(UserMapper.toUser(userDto));
        log.info("Был добавлен новый пользователь, id={}", createdUser.getId());

        return createdUser;
    }

    @Transactional
    @Override
    public User update(Long userId, UserDto userDto) {
        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        User user = UserMapper.toUser(userDto);
        user.setId(userId);

        if (user.getName() == null) {
            user.setName(foundUser.getName());
        }
        if (user.getEmail() == null) {
            user.setEmail(foundUser.getEmail());
        }

        User updatedUser = userRepository.save(user);
        log.info("Пользователь с id {} был обновлен", userId);

        return updatedUser;
    }

    @Override
    public User getById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        log.info("Получен пользователь с id {}: {}", userId, user);

        return user;
    }

    @Override
    public List<User> getAll() {
        List<User> users = userRepository.findAll();
        log.info("Получен список всех пользователей");

        return users;
    }

    @Override
    public void delete(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        userRepository.deleteById(userId);
        log.info("Пользователь с id {} был удален", userId);
    }
}
