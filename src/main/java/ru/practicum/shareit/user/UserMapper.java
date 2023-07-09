package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.dto.ItemBookerDto;
import ru.practicum.shareit.user.model.dto.UserDto;

public class UserMapper {

    public static User toUser(UserDto userDto) {
        return User.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }

    public static ItemBookerDto toItemBookerDto(User user) {
        return ItemBookerDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}
