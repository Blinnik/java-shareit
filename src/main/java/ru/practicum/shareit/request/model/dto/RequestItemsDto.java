package ru.practicum.shareit.request.model.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.item.model.dto.ItemRequestIdDto;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestItemsDto {
    Long id;

    String description;

    LocalDateTime created;

    List<ItemRequestIdDto> items;
}