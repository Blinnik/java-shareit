package ru.practicum.shareit.request.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.common.model.PaginationConfig;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.dto.ItemRequestIdDto;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.model.dto.RequestItemsDto;
import ru.practicum.shareit.request.model.dto.RequestDescription;
import ru.practicum.shareit.request.model.dto.RequestDto;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.request.RequestMapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RequestServiceImpl implements RequestService {
    UserRepository userRepository;
    RequestRepository requestRepository;
    ItemRepository itemRepository;

    @Override
    @Transactional
    public RequestDto create(Long userId, RequestDescription requestDescription) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Request request = Request.builder()
                .description(requestDescription.getDescription())
                .requester(requester)
                .created(LocalDateTime.now())
                .build();

        Request createdRequest = requestRepository.save(request);
        log.info("Был добавлен новый запрос на предмет, id={}", createdRequest.getId());

        return RequestMapper.toRequestDto(createdRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestItemsDto> getOwn(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        List<Request> requests = requestRepository.findAllByRequesterIdOrderByCreatedDesc(userId);
        List<RequestItemsDto> requestItemsDtos =
                RequestMapper.toRequestItemsDto(requests, getRequestItems(requests));
        log.info("Получен список всех запросов пользователя с id {}", userId);

        return requestItemsDtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestItemsDto> getAll(Long userId, PaginationConfig paginationConfig) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        Pageable pageable = paginationConfig.getPageable();
        List<Request> requests =
                requestRepository.findAllByRequesterIdNotOrderByCreatedDesc(userId, pageable).getContent();

        List<RequestItemsDto> requestItemsDtos =
                RequestMapper.toRequestItemsDto(requests, getRequestItems(requests));
        log.info("Получен список всех запросов");

        return requestItemsDtos;
    }

    @Override
    @Transactional(readOnly = true)
    public RequestItemsDto getById(Long userId, Long requestId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id " + requestId + " не найден"));

        RequestItemsDto requestItemsDto = RequestMapper.toRequestItemsDto(request, getRequestItems(request));
        log.info("Получен запрос с id {}: {}", requestId, requestItemsDto);

        return requestItemsDto;
    }

    private List<ItemRequestIdDto> getRequestItems(Request request) {
        return ItemMapper.toItemRequestIdDto(itemRepository.findAllByRequestId(request.getId()));
    }

    private Map<Long, List<ItemRequestIdDto>> getRequestItems(List<Request> requests) {
        Map<Long, List<ItemRequestIdDto>> itemsByRequestId = new HashMap<>();
        for (Request request : requests) {
            itemsByRequestId.put(request.getId(), getRequestItems(request));
        }

        return itemsByRequestId;
    }
}
