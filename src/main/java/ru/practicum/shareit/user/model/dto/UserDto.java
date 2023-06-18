package ru.practicum.shareit.user.model.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import ru.practicum.shareit.marker.ValidationMarker;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
public class UserDto {
    @NotNull(message = "Имя пользователя не может отсутствовать", groups = ValidationMarker.OnCreate.class)
    @Length(message = "Имя пользователя не может быть пустым", min = 1, groups = {
            ValidationMarker.OnCreate.class,
            ValidationMarker.OnUpdate.class})
    String name;
    @NotNull(message = "Email пользователя не может отсутствовать", groups = ValidationMarker.OnCreate.class)
    @Email(message = "Email не соответствует формату", groups = {
            ValidationMarker.OnCreate.class,
            ValidationMarker.OnUpdate.class})
    String email;
}
