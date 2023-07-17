package ru.practicum.shareit.request;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.common.model.PaginationConfig;
import ru.practicum.shareit.request.dto.RequestDescriptionDto;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/requests")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class RequestController {
    RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestBody @Valid RequestDescriptionDto requestDescriptionDto) {
        log.info("Создаем request {}, userId={}", requestDescriptionDto, userId);
        return requestClient.create(userId, requestDescriptionDto);
    }

    @GetMapping
    public ResponseEntity<Object> getOwn(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Получаем все request, ownerId={}", ownerId);
        return requestClient.getOwn(ownerId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @Valid PaginationConfig paginationConfig) {
        Integer from = paginationConfig.getFrom();
        Integer size = paginationConfig.getSize();

        log.info("Получаем все request, userId={}, from={}, size={}", userId, from, size);
        return requestClient.getAll(userId, paginationConfig);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @PathVariable Long requestId) {
        log.info("Получаем request {}, userId={}", requestId, userId);
        return requestClient.getById(userId, requestId);
    }
}
