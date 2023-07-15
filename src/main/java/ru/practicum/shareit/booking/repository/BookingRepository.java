package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long>, QuerydslPredicateExecutor<Booking> {

    @Query("SELECT b " +
            "FROM Booking AS b " +
            "WHERE b.item.id = ?1 AND " +
            "(b.status = 'APPROVED' OR " +
            "b.status = 'WAITING')")
    List<Booking> findAllAcceptedByItemId(Long itemId);

    @Query("SELECT COUNT(b) " +
            "FROM Booking AS b " +
            "WHERE b.item.id = ?1 AND " +
            "b.booker.id = ?2 AND " +
            "b.end < CURRENT_TIMESTAMP AND " +
            "(b.status = 'APPROVED' OR " +
            "b.status = 'WAITING')")
    Integer countAllPrevious(Long itemId, Long bookerId);
}
