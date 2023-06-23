package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long>, QuerydslPredicateExecutor<Booking> {

    @Query("select b " +
            "from Booking as b " +
            "where b.item.id = ?1 and " +
            "(b.status = 'APPROVED' or " +
            "b.status = 'WAITING')")
    List<Booking> findAllAcceptedByItemId(Long itemId);

    @Query("select count(b) " +
            "from Booking as b " +
            "where b.item.id = ?1 and " +
            "b.booker.id = ?2 and " +
            "b.end < current_time and " +
            "(b.status = 'APPROVED' or " +
            "b.status = 'WAITING')")
    Integer countAllPrevious(Long itemId, Long bookerId);
}
