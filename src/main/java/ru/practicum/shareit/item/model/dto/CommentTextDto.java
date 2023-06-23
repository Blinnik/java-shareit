package ru.practicum.shareit.item.model.dto;

import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
public class CommentTextDto {
    @Length(message = "Комментарий должен содержать от 5 до 500 символов", min = 5, max = 500)
    String text;
}
