package ru.practicum.shareit.item.model.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Data
@Builder
public class ItemDto {
    Long id;

    String name;

    String description;

    Boolean available;
}
