package ru.practicum.shareit.user.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.impl.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class UserServiceTest {
    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserServiceImpl userService;

    @Captor
    ArgumentCaptor<User> userArgumentCaptor;

    final User.UserBuilder userBuilder = User.builder()
            .name("Тест")
            .email("test@mail.com");

    final UserDto.UserDtoBuilder userDtoBuilder = UserDto.builder()
            .name("Тест")
            .email("test@mail.com");

    @Test
    void create_whenInvoked_thenReturnUser() {
        User user = userBuilder.build();
        when(userRepository.save(user)).thenReturn(user);

        User createdUser = userService.create(userDtoBuilder.build());

        assertEquals(user, createdUser);
        verify(userRepository, only()).save(user);
        verify(userRepository, times(1)).save(user);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void update_whenUserFoundAndDtoHasNotField_thenUpdateUser() {
        User oldUser = userBuilder.id(1L).build();
        UserDto newUserDto = UserDto.builder()
                .name("Тест2")
                .email("test2@mail.com")
                .build();
        User newUser = User.builder()
                .id(1L)
                .name("Тест2")
                .email("test2@mail.com")
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(oldUser));

        userService.update(1L, newUserDto);

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1))
                .save(userArgumentCaptor.capture());
        verifyNoMoreInteractions(userRepository);
        assertEquals(newUser, userArgumentCaptor.getValue());
    }

    @Test
    void update_whenUserNotFound_thenThrowNotFoundException() {
        UserDto newUserDto = UserDto.builder()
                .name("Тест2")
                .email("test2@mail.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.update(1L, newUserDto));

        verify(userRepository, only()).findById(1L);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any(User.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getById_whenUserFound_thenReturnUser() {
        User user = userBuilder.id(1L).build();
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        User foundUser = userService.getById(1L);

        assertEquals(user, foundUser);
        verify(userRepository, only()).findById(1L);
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getById_whenUserNotFound_thenThrowNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.getById(1L));

        verify(userRepository, only()).findById(1L);
        verify(userRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getAll_whenInvoked_thenReturnUsers() {
        when(userRepository.findAll()).thenReturn(List.of(new User(), new User()));

        List<User> users = userService.getAll();

        assertEquals(2, users.size());
        verify(userRepository, only()).findAll();
        verify(userRepository, times(1)).findAll();
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void delete_whenUserFound_thenDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.delete(1L);

        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void delete_whenUserNotFound_thenThrowNotFoundException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> userService.delete(1L));

        verify(userRepository, only()).existsById(1L);
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, never()).deleteById(anyLong());
        verifyNoMoreInteractions(userRepository);
    }
}