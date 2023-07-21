package ru.practicum.shareit.common.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Setter
@Getter
@ToString
public class PaginationConfig {
    @PositiveOrZero
    Integer from = 0;

    @Positive
    Integer size = 10;
}
