package ru.practicum.shareit.request;

import ru.practicum.shareit.item.model.dto.ItemRequestIdDto;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.model.dto.RequestDto;
import ru.practicum.shareit.request.model.dto.RequestItemsDto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RequestMapper {
    public static RequestDto toRequestDto(Request request) {
        return RequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .build();
    }

    public static RequestItemsDto toRequestItemsDto(Request request, List<ItemRequestIdDto> itemRequestIdDtos) {
        return RequestItemsDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(itemRequestIdDtos)
                .build();
    }

    public static List<RequestItemsDto> toRequestItemsDto(List<Request> requests,
                                                          Map<Long, List<ItemRequestIdDto>> itemsByRequestId) {
        return requests.stream()
                .map((request) -> {
                    List<ItemRequestIdDto> itemRequestIdDtos = itemsByRequestId.get(request.getId());
                    return toRequestItemsDto(request, itemRequestIdDtos);
                })
                .collect(Collectors.toList());
    }
}
