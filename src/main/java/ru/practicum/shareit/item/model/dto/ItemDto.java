package ru.practicum.shareit.item.model.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import ru.practicum.shareit.marker.ValidationMarker;

import javax.validation.constraints.NotNull;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Data
@Builder
public class ItemDto {
    Long id;
    @Length(message = "Название предмета не может быть пустым", min = 1, groups = {
            ValidationMarker.OnCreate.class,
            ValidationMarker.OnUpdate.class})
    @NotNull(message = "Название предмета не может отсутствовать", groups = ValidationMarker.OnCreate.class)
    String name;
    @Length(message = "Название предмета не может быть пустым", min = 1, groups = {
            ValidationMarker.OnCreate.class,
            ValidationMarker.OnUpdate.class})
    @NotNull(message = "Описание предмета не может отсутствовать", groups = ValidationMarker.OnCreate.class)
    String description;
    @NotNull(message = "Статус доступности предмета не может отсутствовать", groups = ValidationMarker.OnCreate.class)
    Boolean available;
}
