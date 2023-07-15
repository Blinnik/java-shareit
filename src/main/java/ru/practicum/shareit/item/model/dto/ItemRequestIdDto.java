package ru.practicum.shareit.item.model.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import ru.practicum.shareit.common.marker.ValidationMarker;

import javax.validation.constraints.NotNull;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Data
@Builder
public class ItemRequestIdDto {
    Long id;

    @Length(message = "Название предмета должно содержать от 1 до 200 символов", min = 1, max = 200, groups = {
            ValidationMarker.OnCreate.class,
            ValidationMarker.OnUpdate.class})
    @NotNull(message = "Название предмета не может отсутствовать", groups = ValidationMarker.OnCreate.class)
    String name;

    @Length(message = "Описание предмета должно содержать от 1 до 1000 символов", min = 1, max = 1000, groups = {
            ValidationMarker.OnCreate.class,
            ValidationMarker.OnUpdate.class})
    @NotNull(message = "Описание предмета не может отсутствовать", groups = ValidationMarker.OnCreate.class)
    String description;

    @NotNull(message = "Статус доступности предмета не может отсутствовать", groups = ValidationMarker.OnCreate.class)
    Boolean available;

    Long requestId;
}
