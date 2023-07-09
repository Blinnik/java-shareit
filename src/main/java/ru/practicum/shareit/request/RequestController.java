package ru.practicum.shareit.request;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.common.model.PaginationConfig;
import ru.practicum.shareit.request.model.dto.RequestDescriptionDto;
import ru.practicum.shareit.request.model.dto.RequestDto;
import ru.practicum.shareit.request.model.dto.RequestItemsDto;
import ru.practicum.shareit.request.service.RequestService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RequestController {
    RequestService requestService;

    @PostMapping
    public RequestDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                             @RequestBody @Valid RequestDescriptionDto requestDescriptionDto) {
        return requestService.create(userId, requestDescriptionDto);
    }

    @GetMapping
    public List<RequestItemsDto> getOwn(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return requestService.getOwn(ownerId);
    }

    @GetMapping("/all")
    public List<RequestItemsDto> getAll(@RequestHeader("X-Sharer-User-Id") Long userId,
                                        @Valid PaginationConfig paginationConfig) {
        return requestService.getAll(userId, paginationConfig);
    }

    @GetMapping("/{requestId}")
    public RequestItemsDto getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                   @PathVariable Long requestId) {
        return requestService.getById(userId, requestId);
    }
}
