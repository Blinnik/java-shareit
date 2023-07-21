package ru.practicum.shareit.item.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.common.exception.BadRequestException;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.common.model.PaginationConfig;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.QItem;
import ru.practicum.shareit.item.model.dto.*;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.impl.ItemServiceImpl;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemServiceTest {
    @Mock
    ItemRepository itemRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    BookingRepository bookingRepository;

    @Mock
    CommentRepository commentRepository;

    @Mock
    RequestRepository requestRepository;

    @InjectMocks
    ItemServiceImpl itemService;

    final LocalDateTime now = LocalDateTime.now();

    final Item.ItemBuilder itemBuilder =
            Item.builder()
                    .id(1L)
                    .name("item")
                    .description("item description")
                    .available(true);

    final ItemRequestIdDto.ItemRequestIdDtoBuilder itemRequestIdDtoBuilder =
            ItemRequestIdDto.builder()
                    .name("item")
                    .description("item description")
                    .available(true);

    final ItemDto.ItemDtoBuilder itemDtoBuilder =
            ItemDto.builder()
                    .id(1L)
                    .name("item")
                    .description("item description")
                    .available(true);

    final User.UserBuilder userBuilder = User.builder()
            .id(1L)
            .name("user")
            .email("user@mail.com");

    final Request.RequestBuilder requestBuilder = Request.builder()
            .id(1L)
            .description("request")
            .created(now);

    final Comment.CommentBuilder commentBuilder = Comment.builder()
            .id(1L)
            .text("Random text")
            .created(now);

    final Booking.BookingBuilder bookingBuilder = Booking.builder()
            .id(1L)
            .status(BookingStatus.WAITING)
            .start(now.minusDays(10))
            .end(now.plusDays(10));

    final ItemBookingsAndCommentsDto.ItemBookingsAndCommentsDtoBuilder itemBookingsAndCommentsDtoBuilder =
            ItemBookingsAndCommentsDto.builder()
                    .id(1L)
                    .name("item")
                    .description("item description")
                    .available(true);

    final PaginationConfig paginationConfig = new PaginationConfig();

    final CommentTextDto commentTextDto = new CommentTextDto("Random text");

    @Test
    void create_whenOwnerFoundAndRequestIdNotDefined_thenReturnItem() {
        ItemRequestIdDto itemRequestIdDto = itemRequestIdDtoBuilder.build();
        User returnedOwner = userBuilder.build();
        Item returnedItem = itemBuilder.owner(returnedOwner).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(returnedOwner));
        when(itemRepository.save(any(Item.class))).thenReturn(returnedItem);

        ItemRequestIdDto actualItem = itemService.create(1L, itemRequestIdDto);
        ItemRequestIdDto expectedItem = ItemMapper.toItemRequestIdDto(returnedItem);

        assertEquals(expectedItem, actualItem);
        verify(requestRepository, never()).findById(anyLong());
        InOrder inOrder = inOrder(userRepository, itemRepository);
        inOrder.verify(userRepository, times(1)).findById(1L);
        inOrder.verify(itemRepository, times(1)).save(any(Item.class));
        verifyNoMoreInteractions(userRepository, itemRepository);
    }

    @Test
    void create_whenOwnerFoundAndRequestIdDefined_thenReturnItemWithRequestId() {
        ItemRequestIdDto itemRequestIdDto =
                itemRequestIdDtoBuilder.requestId(1L).build();
        User owner = userBuilder.build();

        Item item = ItemMapper.toItem(itemRequestIdDto);
        item.setOwner(owner);

        Request request = requestBuilder.build();
        item.setRequest(request);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(itemRepository.save(item)).thenReturn(item);

        ItemRequestIdDto createdItem = itemService.create(1L, itemRequestIdDto);

        ItemRequestIdDto expectedItemDto = itemRequestIdDtoBuilder.requestId(1L).build();
        assertEquals(expectedItemDto, createdItem);
        InOrder inOrder = inOrder(userRepository, requestRepository, itemRepository);
        inOrder.verify(userRepository, times(1)).findById(1L);
        inOrder.verify(requestRepository, times(1)).findById(1L);
        inOrder.verify(itemRepository, times(1)).save(any(Item.class));
        verifyNoMoreInteractions(userRepository, itemRepository, requestRepository);
    }

    @Test
    void create_whenOwnerNotFound_thenThrowNotFoundException() {
        ItemRequestIdDto itemRequestIdDto =
                itemRequestIdDtoBuilder.build();

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException userNotFoundException = assertThrows(NotFoundException.class,
                () -> itemService.create(1L, itemRequestIdDto));

        assertEquals("Пользователь с id 1 не найден", userNotFoundException.getMessage());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, only()).findById(1L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void create_whenRequestNotFound_thenThrowNotFoundException() {
        ItemRequestIdDto itemRequestIdDto =
                itemRequestIdDtoBuilder.requestId(1L).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(requestRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException requestNotFoundException = assertThrows(NotFoundException.class,
                () -> itemService.create(1L, itemRequestIdDto));

        assertEquals("Запрос на предмет с id 1 не найден", requestNotFoundException.getMessage());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, only()).findById(1L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void update_whenUserAndItemFoundAndUserIsAnOwner_thenUpdateItem() {
        ItemDto itemDto = itemDtoBuilder.description("Test2").build();

        User returnedUser = userBuilder.build();
        Item returnedItem = itemBuilder.owner(returnedUser).build();

        Item updatedItem = itemBuilder.owner(returnedUser).description("Test2").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(returnedUser));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(returnedItem));
        when(itemRepository.save(updatedItem)).thenReturn(updatedItem);

        ItemDto actualItemDto = itemService.update(1L, 1L, itemDto);
        ItemDto expectedItemDto = ItemMapper.toItemDto(updatedItem);

        assertEquals(expectedItemDto, actualItemDto);

        InOrder inOrder = inOrder(userRepository, itemRepository);
        inOrder.verify(userRepository, times(1)).findById(1L);
        inOrder.verify(itemRepository, times(1)).findById(1L);
        inOrder.verify(itemRepository, times(1)).save(any(Item.class));
        verifyNoMoreInteractions(userRepository, itemRepository);
    }

    @Test
    void update_whenUserNotFound_thenThrowNotFoundException() {
        ItemDto itemDto = itemDtoBuilder.build();

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException userNotFoundException = assertThrows(NotFoundException.class,
                () -> itemService.update(1L, 1L, itemDto));

        assertEquals("Пользователь с id 1 не найден", userNotFoundException.getMessage());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, only()).findById(1L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void update_whenItemNotFound_thenThrowNotFoundException() {
        ItemDto itemDto = itemDtoBuilder.build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException itemNotFoundException = assertThrows(NotFoundException.class,
                () -> itemService.update(1L, 1L, itemDto));

        assertEquals("Предмет с id 1 не найден", itemNotFoundException.getMessage());
        InOrder inOrder = inOrder(userRepository, itemRepository);
        inOrder.verify(userRepository, times(1)).findById(1L);
        inOrder.verify(itemRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(userRepository, itemRepository);
    }

    @Test
    void update_whenUserNotOwner_thenThrowNotFoundException() {
        ItemDto itemDto = itemDtoBuilder.build();

        User owner = userBuilder.id(100L).build();
        Item returnedItem = itemBuilder.owner(owner).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(returnedItem));

        NotFoundException userNotOwnerException = assertThrows(NotFoundException.class,
                () -> itemService.update(1L, 1L, itemDto));

        assertEquals("Пользователь с id 1 не является владельцем предмета с id 1",
                userNotOwnerException.getMessage());
        InOrder inOrder = inOrder(userRepository, itemRepository);
        inOrder.verify(userRepository, times(1)).findById(1L);
        inOrder.verify(itemRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(userRepository, itemRepository);
    }

    @Test
    void getById_whenItemFoundAndUserNotOwner_thenReturnItemWithoutBookings() {
        Item returnedItem = itemBuilder.owner(User.builder().id(100L).build()).build();
        User commentWriter = userBuilder.build();
        Comment comment = commentBuilder.author(commentWriter).build();
        Comment comment2 = commentBuilder.id(2L).author(commentWriter).build();
        List<Comment> comments = List.of(comment, comment2);

        when(itemRepository.findByIdWithOwner(1L)).thenReturn(Optional.of(returnedItem));
        when(commentRepository.findAllByItemId(1L)).thenReturn(comments);

        ItemBookingsAndCommentsDto actualItem = itemService.getById(1L, 1L);
        ItemBookingsAndCommentsDto expectedItem =
                itemBookingsAndCommentsDtoBuilder
                        .comments(ItemMapper.toCommentDto(comments))
                        .build();

        assertEquals(expectedItem, actualItem);
        InOrder inOrder = inOrder(itemRepository, commentRepository);
        inOrder.verify(itemRepository, times(1)).findByIdWithOwner(1L);
        inOrder.verify(commentRepository, times(1)).findAllByItemId(1L);
        verifyNoMoreInteractions(commentRepository, itemRepository);
    }

    @Test
    void getById_whenItemFoundAndUserOwner_thenReturnItemWithBookings() {
        Item returnedItem = itemBuilder.owner(userBuilder.build()).build();
        User commentWriter = userBuilder.build();

        Comment comment = commentBuilder.author(commentWriter).build();
        Comment comment2 = commentBuilder.id(2L).author(commentWriter).build();
        List<Comment> comments = List.of(comment, comment2);

        User booker1 = userBuilder.id(10L).build();
        User booker2 = userBuilder.id(12L).build();
        Booking lastBooking = bookingBuilder.booker(booker1).build();
        Booking nextBooking = bookingBuilder
                .booker(booker2)
                .start(now.plusDays(2))
                .end(now.plusDays(15))
                .build();
        List<Booking> bookings = List.of(lastBooking, nextBooking);

        when(itemRepository.findByIdWithOwner(1L)).thenReturn(Optional.of(returnedItem));
        when(commentRepository.findAllByItemId(1L)).thenReturn(comments);
        when(bookingRepository.findAllAcceptedByItemId(1L)).thenReturn(bookings);

        ItemBookingsAndCommentsDto actualItem = itemService.getById(1L, 1L);
        ItemBookingsAndCommentsDto expectedItem =
                itemBookingsAndCommentsDtoBuilder
                        .comments(ItemMapper.toCommentDto(comments))
                        .lastBooking(BookingMapper.toBookingBookerIdDto(lastBooking))
                        .nextBooking(BookingMapper.toBookingBookerIdDto(nextBooking))
                        .build();

        assertEquals(expectedItem, actualItem);
        InOrder inOrder = inOrder(itemRepository, commentRepository, bookingRepository);
        inOrder.verify(itemRepository, times(1)).findByIdWithOwner(1L);
        inOrder.verify(commentRepository, times(1)).findAllByItemId(1L);
        inOrder.verify(bookingRepository, times(1)).findAllAcceptedByItemId(1L);
        verifyNoMoreInteractions(commentRepository, itemRepository, bookingRepository);
    }

    @Test
    void getById_whenItemNotFound_thenThrowNotFoundException() {
        assertThrows(NotFoundException.class,
                () -> itemService.getById(1L, 1L));
        verify(itemRepository, times(1)).findByIdWithOwner(1L);
        verify(itemRepository, only()).findByIdWithOwner(1L);
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void getAllByOwnerId_whenUserFound_thenReturnItems() {
        List<Item> returnedItems = List.of(
                itemBuilder.build(),
                itemBuilder.id(2L).name("new test").build()
        );
        Page<Item> pagedItems = new PageImpl<>(returnedItems);

        User booker1 = userBuilder.id(10L).build();
        User booker2 = userBuilder.id(12L).build();
        Booking lastBooking = bookingBuilder.booker(booker1).build();
        Booking nextBooking = bookingBuilder
                .booker(booker2)
                .start(now.plusDays(2))
                .end(now.plusDays(15))
                .build();
        List<Booking> returnedBookings = List.of(lastBooking, nextBooking);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemRepository.findAllByOwnerIdOrderByIdAsc(1L, paginationConfig.getPageable())).thenReturn(pagedItems);
        when(bookingRepository.findAllAcceptedByItemId(anyLong())).thenReturn(returnedBookings);

        List<ItemBookingsDto> actualItemBookingsDto = itemService.getAllByOwnerId(1L, paginationConfig);
        List<ItemBookingsDto> expectedItemBookingsDto = returnedItems.stream()
                .map((x) -> ItemMapper.toItemBookingsDto(x, returnedBookings))
                .collect(Collectors.toList());

        assertEquals(expectedItemBookingsDto, actualItemBookingsDto);
        InOrder inOrder = inOrder(userRepository, itemRepository, bookingRepository);
        inOrder.verify(userRepository, times(1)).existsById(1L);
        inOrder.verify(itemRepository, times(1))
                .findAllByOwnerIdOrderByIdAsc(1L, paginationConfig.getPageable());
        inOrder.verify(bookingRepository, times(2)).findAllAcceptedByItemId(anyLong());
        verifyNoMoreInteractions(userRepository, itemRepository, bookingRepository);
    }

    @Test
    void getAllByOwnerId_whenUserNotFound_thenThrowNotFoundException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> itemService.getAllByOwnerId(1L, paginationConfig));
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, only()).existsById(1L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getAllByTextQuery_whenUserFoundAndTextBlank_thenReturnEmptyList() {
        when(userRepository.existsById(1L)).thenReturn(true);

        List<ItemDto> actualItems = itemService.getAllByTextQuery(1L, "", paginationConfig);

        assertEquals(Collections.emptyList(), actualItems);
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, only()).existsById(1L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getAllByTextQuery_whenUserFoundAndTextNotBlank_thenReturnItems() {
        List<Item> returnedItems = List.of(
                itemBuilder.build(),
                itemBuilder.id(2L).build()
        );
        Page<Item> pagedItems = new PageImpl<>(returnedItems);

        String text = "test";
        BooleanExpression byAvailableTrue = QItem.item.available.isTrue();
        BooleanExpression byNameOrDescriptionContainingText = QItem.item.name.containsIgnoreCase(text)
                .or(QItem.item.description.containsIgnoreCase(text));
        BooleanExpression resExpression = byAvailableTrue.and(byNameOrDescriptionContainingText);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemRepository.findAll(resExpression, paginationConfig.getPageable())).thenReturn(pagedItems);

        List<ItemDto> actualItems = itemService.getAllByTextQuery(1L, text, paginationConfig);
        List<ItemDto> expectedItems = returnedItems.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());

        assertEquals(expectedItems, actualItems);
        assertEquals(2, actualItems.size());
        InOrder inOrder = inOrder(userRepository, itemRepository);
        inOrder.verify(userRepository, times(1)).existsById(1L);
        inOrder.verify(itemRepository, times(1)).findAll(resExpression, paginationConfig.getPageable());
        verifyNoMoreInteractions(userRepository, itemRepository);
    }

    @Test
    void getAllByTextQuery_whenUserNotFound_thenThrowNotFoundException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> itemService.getAllByTextQuery(1L, "test", paginationConfig));
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, only()).existsById(1L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void delete_whenUserAndItemFoundAndUserOwner_thenDeleteItem() {
        Item returnedItem = itemBuilder
                .owner(userBuilder.build())
                .build();

        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemRepository.existsById(1L)).thenReturn(true);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(returnedItem));

        itemService.delete(1L, 1L);

        InOrder inOrder = inOrder(userRepository, itemRepository);
        inOrder.verify(userRepository, times(1)).existsById(1L);
        inOrder.verify(itemRepository, times(1)).existsById(1L);
        inOrder.verify(itemRepository, times(1)).findById(1L);
        inOrder.verify(itemRepository, times(1)).deleteById(1L);
        verifyNoMoreInteractions(userRepository, itemRepository);
    }

    @Test
    void delete_whenUserNotFound_thenThrowNotFoundException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        NotFoundException userNotFoundException = assertThrows(NotFoundException.class,
                () -> itemService.delete(1L, 1L));

        assertEquals("Пользователь с id 1 не найден", userNotFoundException.getMessage());
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, only()).existsById(1L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void delete_whenItemNotFound_thenThrowNotFoundException() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemRepository.existsById(1L)).thenReturn(false);

        NotFoundException itemNotFoundException = assertThrows(NotFoundException.class,
                () -> itemService.delete(1L, 1L));

        assertEquals("Предмет с id 1 не найден", itemNotFoundException.getMessage());
        InOrder inOrder = inOrder(userRepository, itemRepository);
        inOrder.verify(userRepository, times(1)).existsById(1L);
        inOrder.verify(itemRepository, times(1)).existsById(1L);
        verifyNoMoreInteractions(userRepository, itemRepository);
    }

    @Test
    void delete_whenUserNotOwner_thenThrowNotFoundException() {
        Item returnedItem = itemBuilder
                .owner(userBuilder.id(2L).build())
                .build();

        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemRepository.existsById(1L)).thenReturn(true);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(returnedItem));

        NotFoundException itemNotOwnerException = assertThrows(NotFoundException.class,
                () -> itemService.delete(1L, 1L));

        assertEquals("Пользователь с id 1 не является владельцем предмета с id 1",
                itemNotOwnerException.getMessage());
        InOrder inOrder = inOrder(userRepository, itemRepository);
        inOrder.verify(userRepository, times(1)).existsById(1L);
        inOrder.verify(itemRepository, times(1)).existsById(1L);
        inOrder.verify(itemRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(userRepository, itemRepository);
    }

    @Test
    void createComment_whenUserAndItemFoundAndUserHasBookedBefore_thenReturnComment() {
        Item returnedItem = itemBuilder.build();
        User returnedUser = userBuilder.build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(returnedUser));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(returnedItem));
        when(bookingRepository.countAllPrevious(1L, 1L)).thenReturn(1);
        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(i -> {
                    Comment savedComment = (Comment) i.getArguments()[0];
                    return Comment.builder()
                            .id(1L)
                            .text(savedComment.getText())
                            .author(savedComment.getAuthor())
                            .item(savedComment.getItem())
                            .created(savedComment.getCreated())
                            .build();
                });

        CommentDto actualCommentDto = itemService.createComment(1L, 1L, commentTextDto);

        assertEquals(1L, actualCommentDto.getId());
        assertEquals("Random text", actualCommentDto.getText());
        assertEquals("user", actualCommentDto.getAuthorName());
        InOrder inOrder = inOrder(userRepository, itemRepository, bookingRepository, commentRepository);
        inOrder.verify(userRepository, times(1)).findById(1L);
        inOrder.verify(itemRepository, times(1)).findById(1L);
        inOrder.verify(bookingRepository, times(1)).countAllPrevious(1L, 1L);
        inOrder.verify(commentRepository, times(1)).save(any(Comment.class));
        verifyNoMoreInteractions(userRepository, itemRepository, bookingRepository, commentRepository);
    }

    @Test
    void createComment_whenUserNotFound_thenThrowNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException userNotFoundException = assertThrows(NotFoundException.class,
                () -> itemService.createComment(1L, 1L, commentTextDto));

        assertEquals("Пользователь с id 1 не найден", userNotFoundException.getMessage());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, only()).findById(1L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void createComment_whenItemNotFound_thenThrowNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userBuilder.build()));
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException itemNotFoundException = assertThrows(NotFoundException.class,
                () -> itemService.createComment(1L, 1L, commentTextDto));

        assertEquals("Предмет с id 1 не найден", itemNotFoundException.getMessage());
        InOrder inOrder = inOrder(userRepository, itemRepository);
        inOrder.verify(userRepository, times(1)).findById(1L);
        inOrder.verify(itemRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(userRepository, itemRepository);
    }

    @Test
    void createComment_whenUserHasNotBookedBefore_thenThrowBadRequestException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userBuilder.build()));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(itemBuilder.build()));
        when(bookingRepository.countAllPrevious(1L, 1L)).thenReturn(0);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> itemService.createComment(1L, 1L, commentTextDto));

        assertEquals("Пользователь с id 1 раньше не бронировал предмет с id 1",
                badRequestException.getMessage());
        InOrder inOrder = inOrder(userRepository, itemRepository, bookingRepository);
        inOrder.verify(userRepository, times(1)).findById(1L);
        inOrder.verify(itemRepository, times(1)).findById(1L);
        inOrder.verify(bookingRepository, times(1)).countAllPrevious(1L, 1L);
        verifyNoMoreInteractions(userRepository, itemRepository, bookingRepository);
    }
}