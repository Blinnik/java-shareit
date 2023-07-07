package ru.practicum.shareit.booking.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "start_time")
    LocalDateTime start;

    @Column(name = "end_time")
    LocalDateTime end;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    Item item;

    @ManyToOne
    @JoinColumn(name = "booker_id", nullable = false)
    User booker;

    @Enumerated(EnumType.STRING)
    BookingStatus status = BookingStatus.WAITING; // DEFAULT
}
