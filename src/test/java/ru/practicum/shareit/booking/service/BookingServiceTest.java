package ru.practicum.shareit.booking.service;

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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.QBooking;
import ru.practicum.shareit.booking.model.dto.BookingDto;
import ru.practicum.shareit.booking.model.dto.BookingItemIdAndTimeDto;
import ru.practicum.shareit.booking.model.dto.BookingStatusDto;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.impl.BookingServiceImpl;
import ru.practicum.shareit.common.exception.NotAvailableException;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.common.exception.NotOwnerException;
import ru.practicum.shareit.common.model.PaginationConfig;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.dto.ItemBookingsAndCommentsDto;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static ru.practicum.shareit.booking.model.BookingStatus.WAITING;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class BookingServiceTest {
    @Mock
    ItemRepository itemRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    BookingRepository bookingRepository;

    @InjectMocks
    BookingServiceImpl bookingService;

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

    final Booking.BookingBuilder bookingBuilder = Booking.builder()
            .id(1L)
            .start(now.plusDays(10))
            .end(now.plusDays(20))
            .status(WAITING);

    final PaginationConfig paginationConfig = new PaginationConfig();

    final BookingItemIdAndTimeDto bookingItemIdAndTimeDto =
            new BookingItemIdAndTimeDto(1L, now.plusDays(10).toString(), now.plusDays(20).toString());

    @Test
    void create_whenItemFoundAndAvailableAndUserNotOwnerAndFound_thenReturnBookingDto() {
        User owner = userBuilder.id(100L).build();
        Item returnedItem = itemBuilder.owner(owner).build();
        User booker = userBuilder.build();

        Booking savedBooking = bookingBuilder
                .id(null)
                .item(returnedItem)
                .booker(booker)
                .build();

        Booking returnedBooking = bookingBuilder
                .item(returnedItem)
                .booker(booker)
                .build();

        when(itemRepository.findByIdWithOwner(1L)).thenReturn(Optional.of(returnedItem));
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(bookingRepository.save(savedBooking)).thenReturn(returnedBooking);

        BookingDto actualBookingDto = bookingService.create(1L, bookingItemIdAndTimeDto);
        BookingDto expectedBookingDto = BookingMapper.toBookingDto(returnedBooking);

        assertEquals(expectedBookingDto, actualBookingDto);
        InOrder inOrder = inOrder(itemRepository, userRepository, bookingRepository);
        inOrder.verify(itemRepository, times(1)).findByIdWithOwner(1L);
        inOrder.verify(userRepository, times(1)).findById(1L);
        inOrder.verify(bookingRepository, times(1)).save(savedBooking);
        verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void create_whenItemNotFound_thenThrowNotFoundException() {
        when(itemRepository.findByIdWithOwner(1L)).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.create(1L, bookingItemIdAndTimeDto));

        assertEquals("Предмет с id 1 не найден", notFoundException.getMessage());
        verify(itemRepository, times(1)).findByIdWithOwner(1L);
        verify(itemRepository, only()).findByIdWithOwner(1L);
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void create_whenItemNotAvailable_thenThrowNotAvailableException() {
        Item returnedItem = itemBuilder.available(false).build();

        when(itemRepository.findByIdWithOwner(1L)).thenReturn(Optional.of(returnedItem));

        NotAvailableException notAvailableException = assertThrows(NotAvailableException.class,
                () -> bookingService.create(1L, bookingItemIdAndTimeDto));

        assertEquals("Предмет не доступен для брони", notAvailableException.getMessage());
        verify(itemRepository, times(1)).findByIdWithOwner(1L);
        verify(itemRepository, only()).findByIdWithOwner(1L);
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void create_whenUserOwner_thenThrowNotFoundException() {
        User owner = userBuilder.build();
        Item returnedItem = itemBuilder.owner(owner).build();

        when(itemRepository.findByIdWithOwner(1L)).thenReturn(Optional.of(returnedItem));

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.create(1L, bookingItemIdAndTimeDto));

        assertEquals("Пользователь не может забронировать собственный предмет", notFoundException.getMessage());
        verify(itemRepository, times(1)).findByIdWithOwner(1L);
        verify(itemRepository, only()).findByIdWithOwner(1L);
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void create_whenUserNotFound_thenThrowNotFoundException() {
        User owner = userBuilder.id(100L).build();
        Item returnedItem = itemBuilder.owner(owner).build();

        when(itemRepository.findByIdWithOwner(1L)).thenReturn(Optional.of(returnedItem));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.create(1L, bookingItemIdAndTimeDto));

        assertEquals("Пользователь с id 1 не найден", notFoundException.getMessage());
        InOrder inOrder = inOrder(itemRepository, userRepository);
        inOrder.verify(itemRepository, times(1)).findByIdWithOwner(1L);
        inOrder.verify(userRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(itemRepository, userRepository);
    }

    @Test
    void updateStatus_whenBookingFoundAndItemFoundAndUserOwnerAndActionNotRepeated_thenReturnBookingDto() {
        User itemOwner = userBuilder.build();
        Item bookingItem = itemBuilder.owner(itemOwner).build();
        User booker = userBuilder.id(10L).build();
        Booking returnedBooking = bookingBuilder
                .item(bookingItem)
                .status(BookingStatus.WAITING)
                .booker(booker)
                .build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(returnedBooking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(bookingItem));
        when(bookingRepository.save(returnedBooking)).thenReturn(returnedBooking);

        BookingDto actualBookingDto = bookingService.updateStatus(1L, 1L, true);
        BookingDto expectedBookingDto = BookingMapper.toBookingDto(returnedBooking);

        assertEquals(expectedBookingDto, actualBookingDto);
        verify(bookingRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).findById(1L);
        verify(bookingRepository, times(1)).save(returnedBooking);
        verifyNoMoreInteractions(bookingRepository, itemRepository);
    }

    @Test
    void updateStatus_whenBookingNotFound_thenThrowNotFoundException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.updateStatus(1L, 1L, true));

        assertEquals("Бронь с id 1 не найдена", notFoundException.getMessage());
        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingRepository, only()).findById(1L);
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void updateStatus_whenItemNotFound_thenThrowNotFoundException() {
        Item bookingItem = itemBuilder.build();
        Booking returnedBooking = bookingBuilder.item(bookingItem).build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(returnedBooking));
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.updateStatus(1L, 1L, true));

        assertEquals("Предмет с id 1 не найден", notFoundException.getMessage());
        InOrder inOrder = inOrder(bookingRepository, itemRepository);
        inOrder.verify(bookingRepository, times(1)).findById(1L);
        inOrder.verify(itemRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(bookingRepository, itemRepository);
    }

    @Test
    void updateStatus_whenUserNotOwner_thenThrowNotOwnerException() {
        User itemOwner = userBuilder.id(100L).build();
        Item bookingItem = itemBuilder.owner(itemOwner).build();
        Booking returnedBooking = bookingBuilder.item(bookingItem).build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(returnedBooking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(bookingItem));

        NotOwnerException notOwnerException = assertThrows(NotOwnerException.class,
                () -> bookingService.updateStatus(1L, 1L, true));

        assertEquals("Пользователь с id 1 не является владельцем вещи с id 1", notOwnerException.getMessage());
        InOrder inOrder = inOrder(bookingRepository, itemRepository);
        inOrder.verify(bookingRepository, times(1)).findById(1L);
        inOrder.verify(itemRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(bookingRepository, itemRepository);
    }

    @Test
    void updateStatus_whenActionRepeated_thenThrowNotAvailableException() {
        User itemOwner = userBuilder.build();
        Item bookingItem = itemBuilder.owner(itemOwner).build();
        Booking returnedBooking = bookingBuilder.status(BookingStatus.APPROVED).item(bookingItem).build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(returnedBooking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(bookingItem));

        NotAvailableException notAvailableException = assertThrows(NotAvailableException.class,
                () -> bookingService.updateStatus(1L, 1L, true));

        assertEquals("Нельзя повторно одобрить бронь", notAvailableException.getMessage());
        InOrder inOrder = inOrder(bookingRepository, itemRepository);
        inOrder.verify(bookingRepository, times(1)).findById(1L);
        inOrder.verify(itemRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(bookingRepository, itemRepository);
    }

    @Test
    void getById_whenBookingFoundAndUserOwnerOrBooker_thenReturnBookingDto() {
        User itemOwner = userBuilder.build();
        Item bookingItem = itemBuilder.owner(itemOwner).build();
        User booker = userBuilder.id(200L).build();
        Booking returnedBooking = bookingBuilder
                .status(BookingStatus.APPROVED)
                .booker(booker)
                .item(bookingItem)
                .build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(returnedBooking));

        BookingDto actualBookingDto = bookingService.getById(1L, 1L);
        BookingDto expectedBookingDto = BookingMapper.toBookingDto(returnedBooking);

        assertEquals(expectedBookingDto, actualBookingDto);
        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingRepository, only()).findById(1L);
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void getById_whenNotFound_thenThrowNotFoundException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.getById(1L, 1L));

        assertEquals("Бронь с id 1 не найдена", notFoundException.getMessage());
        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingRepository, only()).findById(1L);
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void getById_whenUserNotOwnerAndNotBooker_thenThrowNotFoundException() {
        User itemOwner = userBuilder.id(100L).build();
        Item bookingItem = itemBuilder.owner(itemOwner).build();
        User booker = userBuilder.id(200L).build();
        Booking returnedBooking = bookingBuilder
                .status(BookingStatus.APPROVED)
                .booker(booker)
                .item(bookingItem)
                .build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(returnedBooking));

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.getById(1L, 1L));

        assertEquals("Нет броней, связанных с пользователем с id 1", notFoundException.getMessage());
        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingRepository, only()).findById(1L);
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void getAllByOwnerId_whenBookingsListNotEmpty_thenReturnBookingDtoList() {
        Sort byStartDesc = Sort.by("start").descending();
        Pageable pageable = paginationConfig.getPageable(byStartDesc);

        QBooking qBooking = QBooking.booking;
        BooleanExpression byOwnerId = qBooking.item.owner.id.eq(1L);
        BooleanExpression byStatus = qBooking.status.eq(WAITING);
        BooleanExpression resExpression = byOwnerId.and(byStatus);

        Item bookingItem = itemBuilder.build();
        Item bookingItem2 = itemBuilder.id(2L).build();
        User booker = userBuilder.build();
        User booker2 = userBuilder.id(2L).build();

        List<Booking> returnedBookings = List.of(
                bookingBuilder.status(WAITING).item(bookingItem).booker(booker).build(),
                bookingBuilder.id(2L).status(WAITING).item(bookingItem2).booker(booker2).build()
        );
        Page<Booking> pagedBookings = new PageImpl<>(returnedBookings);

        when(bookingRepository.findAll(resExpression, pageable))
                .thenReturn(pagedBookings);

        List<BookingDto> actualBookingDtos =
                bookingService.getAllByOwnerId(1L, BookingStatusDto.WAITING, paginationConfig);
        List<BookingDto> expectedBookingDtos = BookingMapper.toBookingDto(returnedBookings);

        assertEquals(expectedBookingDtos, actualBookingDtos);
        verify(bookingRepository, times(1))
                .findAll(resExpression, pageable);
        verify(bookingRepository, only()).findAll(resExpression, pageable);
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void getAllByOwnerId_whenBookingListNotEmpty_thenThrowNotFoundException() {
        Sort byStartDesc = Sort.by("start").descending();
        Pageable pageable = paginationConfig.getPageable(byStartDesc);

        QBooking qBooking = QBooking.booking;
        BooleanExpression byOwnerId = qBooking.item.owner.id.eq(1L);
        BooleanExpression byStatus = qBooking.status.eq(WAITING);
        BooleanExpression resExpression = byOwnerId.and(byStatus);

        when(bookingRepository.findAll(resExpression, pageable))
                .thenReturn(Page.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.getAllByOwnerId(1L, BookingStatusDto.WAITING, paginationConfig));

        assertEquals("По характеристике WAITING не было найдено вещей, забронированных у пользователя с id 1",
                notFoundException.getMessage());
        verify(bookingRepository, times(1))
                .findAll(resExpression, pageable);
        verify(bookingRepository, only()).findAll(resExpression, pageable);
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void getAllByBookerId_whenBookingsListNotEmpty_thenReturnBookingDtoList() {
        Sort byStartDesc = Sort.by("start").descending();
        Pageable pageable = paginationConfig.getPageable(byStartDesc);

        QBooking qBooking = QBooking.booking;
        BooleanExpression byBookerId = qBooking.booker.id.eq(1L);
        BooleanExpression byStatus = qBooking.status.eq(WAITING);
        BooleanExpression resExpression = byBookerId.and(byStatus);

        Item bookingItem = itemBuilder.build();
        Item bookingItem2 = itemBuilder.id(2L).build();
        User booker = userBuilder.build();
        User booker2 = userBuilder.id(2L).build();

        List<Booking> returnedBookings = List.of(
                bookingBuilder.status(WAITING).item(bookingItem).booker(booker).build(),
                bookingBuilder.id(2L).status(WAITING).item(bookingItem2).booker(booker2).build()
        );
        Page<Booking> pagedBookings = new PageImpl<>(returnedBookings);

        when(bookingRepository.findAll(resExpression, pageable))
                .thenReturn(pagedBookings);

        List<BookingDto> actualBookingDtos =
                bookingService.getAllByBookerId(1L, BookingStatusDto.WAITING, paginationConfig);
        List<BookingDto> expectedBookingDtos = BookingMapper.toBookingDto(returnedBookings);

        assertEquals(expectedBookingDtos, actualBookingDtos);
        verify(bookingRepository, times(1))
                .findAll(resExpression, pageable);
        verify(bookingRepository, only()).findAll(resExpression, pageable);
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void getAllByBookerId_whenBookingListNotEmpty_thenThrowNotFoundException() {
        Sort byStartDesc = Sort.by("start").descending();
        Pageable pageable = paginationConfig.getPageable(byStartDesc);

        QBooking qBooking = QBooking.booking;
        BooleanExpression byBookerId = qBooking.booker.id.eq(1L);
        BooleanExpression byStatus = qBooking.status.eq(WAITING);
        BooleanExpression resExpression = byBookerId.and(byStatus);

        when(bookingRepository.findAll(resExpression, pageable))
                .thenReturn(Page.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.getAllByBookerId(1L, BookingStatusDto.WAITING, paginationConfig));

        assertEquals("По характеристике WAITING не было найдено вещей, забронированных пользователем с id 1",
                notFoundException.getMessage());
        verify(bookingRepository, times(1))
                .findAll(resExpression, pageable);
        verify(bookingRepository, only()).findAll(resExpression, pageable);
        verifyNoMoreInteractions(bookingRepository);
    }
}