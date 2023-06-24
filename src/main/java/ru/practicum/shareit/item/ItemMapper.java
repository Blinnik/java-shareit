package ru.practicum.shareit.item;

import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.dto.*;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

    public static Item toItem(ItemDto itemDto) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .build();
    }

    public static ItemBookingsDto toItemBookingsDto(Item item, List<Booking> bookingsOfItem) {
        Booking lastBooking = null;
        Booking nextBooking = null;

        LocalDateTime now = LocalDateTime.now();

        for (Booking currentBooking : bookingsOfItem) {
            if (currentBooking.getStatus().equals(BookingStatus.REJECTED)) {
                continue;
            }

            LocalDateTime curBookingStart = currentBooking.getStart();

            if (lastBooking == null && curBookingStart.isBefore(now)) {
                lastBooking = currentBooking;
            } else if (lastBooking != null && curBookingStart.isBefore(now) &&
                    curBookingStart.isAfter(lastBooking.getStart())) {
                lastBooking = currentBooking;
            }

            if (nextBooking == null && curBookingStart.isAfter(now)) {
                nextBooking = currentBooking;
            } else if (nextBooking != null && curBookingStart.isAfter(now) &&
                    curBookingStart.isBefore(nextBooking.getStart())) {
                nextBooking = currentBooking;
            }
        }

        return new ItemBookingsDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                BookingMapper.toBookingBookerIdDto(lastBooking),
                BookingMapper.toBookingBookerIdDto(nextBooking)
        );
    }

    public static ItemBookingsAndCommentsDto toItemBookingsAndCommentsDto(Item item,
                                                  List<Booking> bookingsOfItem,
                                                  List<Comment> comments) {
        ItemBookingsDto itemBookingsDto = toItemBookingsDto(item, bookingsOfItem);

        return new ItemBookingsAndCommentsDto(
                itemBookingsDto.getId(),
                itemBookingsDto.getName(),
                itemBookingsDto.getDescription(),
                itemBookingsDto.getAvailable(),
                itemBookingsDto.getLastBooking(),
                itemBookingsDto.getNextBooking(),
                ItemMapper.toCommentDto(comments)
        );
    }

    public static ItemBookingsAndCommentsDto toItemBookingsAndCommentsDto(Item item, List<Comment> comments) {
        return new ItemBookingsAndCommentsDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                null,
                null,
                ItemMapper.toCommentDto(comments)
        );
    }

    public static CommentDto toCommentDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getCreated()
        );
    }

    public static List<CommentDto> toCommentDto(List<Comment> comments) {
        return comments.stream()
                .map(ItemMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    public static Comment toComment(Long userId, CommentTextDto commentTextDto) {
        return Comment.builder()
                .text(commentTextDto.getText())
                .author(User.builder().id(userId).build())
                .build();
    }
}
