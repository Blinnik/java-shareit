package ru.practicum.shareit.common.model;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Setter
public class PaginationConfig {
    @PositiveOrZero
    Integer from = 0;

    @Positive
    Integer size = 10;

    public Pageable getPageable() {
        return PageRequest.of(from / size, size);
    }

    public Pageable getPageable(Sort sort) {
        return PageRequest.of(from / size, size, sort);
    }
}
