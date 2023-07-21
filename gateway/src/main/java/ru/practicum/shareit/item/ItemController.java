package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.common.marker.ValidationMarker;
import ru.practicum.shareit.common.model.PaginationConfig;
import ru.practicum.shareit.item.dto.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/items")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Validated
public class ItemController {
    ItemClient itemClient;

    @PostMapping
    @Validated(ValidationMarker.OnCreate.class)
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestBody @Valid ItemRequestIdDto itemRequestIdDto) {
        return itemClient.create(userId, itemRequestIdDto);
    }

    @PatchMapping("/{itemId}")
    @Validated(ValidationMarker.OnUpdate.class)
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @PathVariable Long itemId,
                                         @RequestBody @Valid ItemDto itemDto) {
        return itemClient.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @PathVariable Long itemId) {
        return itemClient.getById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByOwnerId(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                                  @Valid PaginationConfig paginationConfig) {
        return itemClient.getAllByOwnerId(ownerId, paginationConfig);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> getAllByTextQuery(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                    @RequestParam String text,
                                                    @Valid PaginationConfig paginationConfig) {
        return itemClient.getAllByTextQuery(userId, text, paginationConfig);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> deleteItem(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId) {
        return itemClient.deleteItem(userId, itemId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @PathVariable Long itemId,
                                                @RequestBody @Valid CommentTextDto commentTextDto) {
        return itemClient.createComment(userId, itemId, commentTextDto);
    }
}
