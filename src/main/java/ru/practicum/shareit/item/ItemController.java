package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.common.model.PaginationConfig;
import ru.practicum.shareit.item.model.dto.*;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.common.marker.ValidationMarker;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Validated
public class ItemController {
    ItemService itemService;

    @PostMapping
    @Validated(ValidationMarker.OnCreate.class)
    public ItemRequestIdDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                   @RequestBody @Valid ItemRequestIdDto itemRequestIdDto) {

        return itemService.create(userId, itemRequestIdDto);
    }

    @PatchMapping("/{itemId}")
    @Validated(ValidationMarker.OnUpdate.class)
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @PathVariable Long itemId,
                          @RequestBody @Valid ItemDto itemDto) {

        return itemService.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemBookingsAndCommentsDto getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @PathVariable Long itemId) {
        return itemService.getById(userId, itemId);
    }

    @GetMapping
    public List<ItemBookingsDto> getAllByOwnerId(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                                 @Valid PaginationConfig paginationConfig) {
        return itemService.getAllByOwnerId(ownerId, paginationConfig);
    }

    @GetMapping("/search")
    public List<ItemDto> getAllByTextQuery(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @RequestParam String text,
                                           @Valid PaginationConfig paginationConfig) {
        return itemService.getAllByTextQuery(userId, text, paginationConfig);
    }

    @DeleteMapping("/{itemId}")
    public void delete(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId) {
        itemService.delete(userId, itemId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                    @PathVariable Long itemId,
                                    @RequestBody @Valid CommentTextDto commentTextDto) {
        return itemService.createComment(userId, itemId, commentTextDto);
    }
}
