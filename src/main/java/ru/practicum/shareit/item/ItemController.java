package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.marker.ValidationMarker;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Validated
public class ItemController {
    ItemService itemService;

    @PostMapping
    @Validated(ValidationMarker.OnCreate.class)
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @RequestBody @Valid ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto);
        return ItemMapper.toItemDto(itemService.create(userId, item));
    }

    @PatchMapping("/{itemId}")
    @Validated(ValidationMarker.OnUpdate.class)
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @PathVariable Long itemId,
                          @RequestBody @Valid ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto);
        return ItemMapper.toItemDto(itemService.update(userId, itemId, item));
    }

    @GetMapping("/{itemId}")
    public ItemDto getOne(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId) {
        return ItemMapper.toItemDto(itemService.getOne(userId, itemId));
    }

    @GetMapping
    public List<ItemDto> getMany(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemService.getMany(ownerId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/search")
    public List<ItemDto> getManyByTextQuery(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestParam String text) {
        return itemService.getManyByTextQuery(userId, text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{itemId}")
    public void delete(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId) {
        itemService.delete(userId, itemId);
    }
}
