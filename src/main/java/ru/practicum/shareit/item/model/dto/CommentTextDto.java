package ru.practicum.shareit.item.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
public class CommentTextDto {
    @Length(message = "Комментарий должен содержать от 5 до 500 символов", min = 5, max = 500)
    String text;
}
