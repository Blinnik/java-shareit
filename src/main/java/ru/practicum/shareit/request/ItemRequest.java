package ru.practicum.shareit.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.user.model.User;

import java.time.Instant;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class ItemRequest {
    Long id;
    String description;
    User requester;
    Instant created;
}
