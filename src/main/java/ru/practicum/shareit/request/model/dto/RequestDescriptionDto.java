package ru.practicum.shareit.request.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestDescriptionDto {
    @NotNull
    @Length(message = "Описание запроса на предмет должно содержать от 10 до 500 символов", min = 10, max = 500)
    String description;
}
