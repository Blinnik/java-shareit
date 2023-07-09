package ru.practicum.shareit.request.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.common.model.PaginationConfig;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.dto.ItemRequestIdDto;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.model.dto.RequestDescriptionDto;
import ru.practicum.shareit.request.model.dto.RequestDto;
import ru.practicum.shareit.request.model.dto.RequestItemsDto;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.request.service.impl.RequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class RequestServiceTest {
    @Mock
    ItemRepository itemRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    RequestRepository requestRepository;

    @InjectMocks
    RequestServiceImpl requestService;

    final LocalDateTime now = LocalDateTime.now();

    final Item.ItemBuilder itemBuilder =
            Item.builder()
                    .id(1L)
                    .name("item")
                    .description("item description")
                    .available(true);

    final User.UserBuilder userBuilder = User.builder()
            .id(1L)
            .name("user")
            .email("user@mail.com");

    final PaginationConfig paginationConfig = new PaginationConfig();

    final RequestDescriptionDto requestDescriptionDto = new RequestDescriptionDto("Test description");

    final Request.RequestBuilder requestBuilder = Request.builder()
            .description(requestDescriptionDto.getDescription());

    @Test
    void create_whenUserFound_thenReturnRequest() {
        User requester = userBuilder.build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(requestRepository.save(any(Request.class)))
                .thenAnswer((i) -> {
                    Request returnedRequest = (Request) i.getArguments()[0];
                    returnedRequest.setId(1L);
                    return returnedRequest;
                });

        RequestDto actualRequestDto = requestService.create(1L, requestDescriptionDto);

        assertEquals(1L, actualRequestDto.getId());
        assertEquals(requestDescriptionDto.getDescription(), actualRequestDto.getDescription());
        InOrder inOrder = inOrder(userRepository, requestRepository);
        inOrder.verify(userRepository, times(1)).findById(1L);
        inOrder.verify(requestRepository, times(1)).save(any(Request.class));
        verifyNoMoreInteractions(userRepository, requestRepository);
    }

    @Test
    void create_whenUserNotFound_thenThrowNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> requestService.create(1L, requestDescriptionDto));

        assertEquals("Пользователь с id 1 не найден", notFoundException.getMessage());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, only()).findById(1L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getOwn_whenUserFound_thenReturnRequestItemsDtoList() {
        User requester = userBuilder.build();
        User requester2 = userBuilder.id(2L).build();
        Request request = requestBuilder.id(1L).requester(requester).created(now).build();
        Request request2 = requestBuilder.id(2L).requester(requester2).created(now).build();
        List<Request> returnedRequests = List.of(request, request2);

        User itemOwner = userBuilder.id(100L).build();
        User itemOwner2 = userBuilder.id(200L).build();
        Item returnedItem = itemBuilder.owner(itemOwner).request(request).build();
        Item returnedItem2 = itemBuilder.id(2L).owner(itemOwner2).request(request2).build();


        when(userRepository.existsById(1L)).thenReturn(true);
        when(requestRepository.findAllByRequesterIdOrderByCreatedDesc(1L)).thenReturn(returnedRequests);
        when(itemRepository.findAllByRequestId(1L)).thenReturn(List.of(returnedItem));
        when(itemRepository.findAllByRequestId(2L)).thenReturn(List.of(returnedItem2));

        List<RequestItemsDto> actualRequestItemsDtos = requestService.getOwn(1L);

        List<ItemRequestIdDto> itemRequestIdDtos = List.of(ItemMapper.toItemRequestIdDto(returnedItem));
        List<ItemRequestIdDto> itemRequestIdDtos2 = List.of(ItemMapper.toItemRequestIdDto(returnedItem2));
        List<RequestItemsDto> expectedRequestItemsDtos = List.of(
                RequestItemsDto.builder().id(1L).items(itemRequestIdDtos)
                        .description("Test description").created(now).build(),

                RequestItemsDto.builder().id(2L).items(itemRequestIdDtos2)
                        .description("Test description").created(now).build()
        );

        assertEquals(expectedRequestItemsDtos, actualRequestItemsDtos);
        InOrder inOrder = inOrder(userRepository, requestRepository, itemRepository);
        inOrder.verify(userRepository, times(1)).existsById(1L);
        inOrder.verify(requestRepository, times(1))
                .findAllByRequesterIdOrderByCreatedDesc(1L);
        inOrder.verify(itemRepository, times(2))
                .findAllByRequestId(anyLong());
        verifyNoMoreInteractions(userRepository, requestRepository, itemRepository);
    }

    @Test
    void getOwn_whenUserNotFound_thenThrowNotFoundException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> requestService.getOwn(1L));

        assertEquals("Пользователь с id 1 не найден", notFoundException.getMessage());
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, only()).existsById(1L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getAll_whenUserFound_thenReturnRequestItemsDtoList() {
        Pageable pageable = paginationConfig.getPageable();

        User requester = userBuilder.build();
        User requester2 = userBuilder.id(2L).build();
        Request request = requestBuilder.id(1L).requester(requester).created(now).build();
        Request request2 = requestBuilder.id(2L).requester(requester2).created(now).build();
        List<Request> returnedRequests = List.of(request, request2);

        User itemOwner = userBuilder.id(100L).build();
        User itemOwner2 = userBuilder.id(200L).build();
        Item returnedItem = itemBuilder.owner(itemOwner).request(request).build();
        Item returnedItem2 = itemBuilder.id(2L).owner(itemOwner2).request(request2).build();


        when(userRepository.existsById(1L)).thenReturn(true);
        when(requestRepository.findAllByRequesterIdNotOrderByCreatedDesc(1L, pageable))
                .thenReturn(new PageImpl<>(returnedRequests));
        when(itemRepository.findAllByRequestId(1L)).thenReturn(List.of(returnedItem));
        when(itemRepository.findAllByRequestId(2L)).thenReturn(List.of(returnedItem2));

        List<RequestItemsDto> actualRequestItemsDtos = requestService.getAll(1L, paginationConfig);

        List<ItemRequestIdDto> itemRequestIdDtos = List.of(ItemMapper.toItemRequestIdDto(returnedItem));
        List<ItemRequestIdDto> itemRequestIdDtos2 = List.of(ItemMapper.toItemRequestIdDto(returnedItem2));
        List<RequestItemsDto> expectedRequestItemsDtos = List.of(
                RequestItemsDto.builder().id(1L).items(itemRequestIdDtos)
                        .description("Test description").created(now).build(),

                RequestItemsDto.builder().id(2L).items(itemRequestIdDtos2)
                        .description("Test description").created(now).build()
        );

        assertEquals(expectedRequestItemsDtos, actualRequestItemsDtos);
        InOrder inOrder = inOrder(userRepository, requestRepository, itemRepository);
        inOrder.verify(userRepository, times(1)).existsById(1L);
        inOrder.verify(requestRepository, times(1))
                .findAllByRequesterIdNotOrderByCreatedDesc(1L, pageable);
        inOrder.verify(itemRepository, times(2))
                .findAllByRequestId(anyLong());
        verifyNoMoreInteractions(userRepository, requestRepository, itemRepository);
    }

    @Test
    void getAll_whenUserNotFound_thenThrowNotFoundException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> requestService.getAll(1L, paginationConfig));

        assertEquals("Пользователь с id 1 не найден", notFoundException.getMessage());
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, only()).existsById(1L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getById_whenUserFound_thenReturnRequestItemsDto() {
        User requester = userBuilder.build();
        Request returnedRequest = requestBuilder.id(1L).requester(requester).created(now).build();

        User itemOwner = userBuilder.id(100L).build();
        Item returnedItem = itemBuilder.owner(itemOwner).request(returnedRequest).build();

        when(userRepository.existsById(1L)).thenReturn(true);
        when(requestRepository.findById(1L))
                .thenReturn(Optional.of(returnedRequest));
        when(itemRepository.findAllByRequestId(1L)).thenReturn(List.of(returnedItem));

        RequestItemsDto actualRequestItemsDto = requestService.getById(1L, 1L);

        List<ItemRequestIdDto> itemRequestIdDtos = List.of(ItemMapper.toItemRequestIdDto(returnedItem));
        RequestItemsDto expectedRequestItemsDto = RequestItemsDto.builder().id(1L).items(itemRequestIdDtos)
                        .description("Test description").created(now).build();

        assertEquals(expectedRequestItemsDto, actualRequestItemsDto);
        InOrder inOrder = inOrder(userRepository, requestRepository, itemRepository);
        inOrder.verify(userRepository, times(1)).existsById(1L);
        inOrder.verify(requestRepository, times(1))
                .findById(1L);
        inOrder.verify(itemRepository, times(1)).findAllByRequestId(anyLong());
        verifyNoMoreInteractions(userRepository, requestRepository, itemRepository);
    }

    @Test
    void getById_whenUserNotFound_thenThrowNotFoundException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> requestService.getById(1L, 1L));

        assertEquals("Пользователь с id 1 не найден", notFoundException.getMessage());
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, only()).existsById(1L);
        verifyNoMoreInteractions(userRepository);
    }
}