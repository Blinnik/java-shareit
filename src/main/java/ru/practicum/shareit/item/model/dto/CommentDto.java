package ru.practicum.shareit.item.model.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Data
public class CommentDto {
    Long id;
    String text;
    String authorName;
    LocalDateTime created;
}