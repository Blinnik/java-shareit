package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long>, QuerydslPredicateExecutor<Item> {
    @Query("select i " +
            "from Item as i " +
            "join fetch i.owner " +
            "where i.id = ?1 ")
    Optional<Item> findByIdWithOwner(Long id);

    List<Item> findAllByOwnerId(Long ownerId);
}

