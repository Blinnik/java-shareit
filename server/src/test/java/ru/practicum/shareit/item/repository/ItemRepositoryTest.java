package ru.practicum.shareit.item.repository;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemRepositoryTest {
    @Autowired
    TestEntityManager em;

    @Autowired
    ItemRepository itemRepository;

    @Test
    void findByIdWithOwner() {
        User savedOwner = User.builder()
                .name("test2")
                .email("test2@mail.com")
                .build();

        assertNull(savedOwner.getId());
        em.persist(savedOwner);
        assertNotNull(savedOwner.getId());

        Item savedItem = Item.builder()
                .name("Test name")
                .description("Test description")
                .owner(savedOwner)
                .available(true)
                .build();

        assertNull(savedItem.getId());
        em.persist(savedItem);
        assertNotNull(savedItem.getId());

        Optional<Item> foundItemOpt = itemRepository.findByIdWithOwner(savedItem.getId());

        assertTrue(foundItemOpt.isPresent());

        Item foundItem = foundItemOpt.get();

        assertEquals(savedItem, foundItem);
        assertEquals(savedOwner, foundItem.getOwner());
    }
}