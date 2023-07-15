package ru.practicum.shareit.item.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.QComment;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QCommentTest {
    @Autowired
    TestEntityManager em;

    @Autowired
    CommentRepository commentRepository;

    LocalDateTime now;

    Item item3;
    User commentator;
    Comment comment1;
    Comment comment2;
    Comment comment3;

    // QComment нигде не используется, но генерируется автоматически QueryDSL, из-за чего страдает покрытие
    // Поэтому я добавил тесты для проверки функциональности QComment
    @BeforeEach
    void setUp() {
        User itemOwner = User.builder().name("owner").email("owner@mail.com").build();
        em.persist(itemOwner);

        Item item1 = Item.builder()
                .name("дрель")
                .description("item description")
                .owner(itemOwner)
                .available(true)
                .build();
        em.persist(item1);

        Item item2 = Item.builder()
                .name("шуруповерт")
                .description("item description2")
                .owner(itemOwner)
                .available(true)
                .build();
        em.persist(item2);

        item3 = Item.builder()
                .name("пылесос")
                .description("item description3")
                .owner(itemOwner)
                .available(true)
                .build();
        em.persist(item3);

        commentator = User.builder().name("commentator").email("commentator@mail.com").build();
        em.persist(commentator);

        now = LocalDateTime.now();

        comment1 = Comment.builder()
                .text("Славный пылесос")
                .author(commentator)
                .item(item3)
                .created(now.plusHours(1))
                .build();

        comment2 = Comment.builder()
                .text("Хорошая дрель!")
                .author(commentator)
                .item(item1)
                .created(now.minusDays(10))
                .build();

        comment3 = Comment.builder()
                .text("Ужасный шуруповерт!")
                .author(commentator)
                .item(item2)
                .created(now.plusHours(5))
                .build();

        em.persist(comment1);
        em.persist(comment2);
        em.persist(comment3);
    }

    @Test
    void getCommentsByCommentId() {
        BooleanExpression byCommentId = QComment.comment.id.eq(comment2.getId());

        List<Comment> actualCommentsByCommentId = Lists.newArrayList(commentRepository.findAll(byCommentId));
        List<Comment> expectedCommentsByCommentId = List.of(comment2);

        assertEquals(expectedCommentsByCommentId, actualCommentsByCommentId);
    }

    @Test
    void getCommentsByAuthorId() {
        BooleanExpression byAuthorId = QComment.comment.author.id.eq(commentator.getId());

        List<Comment> actualCommentsByAuthorId = Lists.newArrayList(commentRepository.findAll(byAuthorId));
        List<Comment> expectedCommentsByAuthorId = List.of(comment1, comment2, comment3);

        assertEquals(expectedCommentsByAuthorId, actualCommentsByAuthorId);
    }

    @Test
    void getCommentsByText() {
        BooleanExpression byText = QComment.comment.text.containsIgnoreCase("ПЫЛ");

        List<Comment> actualCommentsByText = Lists.newArrayList(commentRepository.findAll(byText));
        List<Comment> expectedCommentsByText = List.of(comment1);

        assertEquals(expectedCommentsByText, actualCommentsByText);
    }

    @Test
    void getCommentsByDateCreated() {
        BooleanExpression byDateCreated = QComment.comment.created.before(now);

        List<Comment> actualCommentsByDateCreated = Lists.newArrayList(commentRepository.findAll(byDateCreated));
        List<Comment> expectedCommentsByDateCreated = List.of(comment2);

        assertEquals(expectedCommentsByDateCreated, actualCommentsByDateCreated);
    }

    @Test
    void getCommentsByItemId() {
        BooleanExpression byItemId = QComment.comment.item.id.eq(item3.getId());

        List<Comment> actualCommentsByItemId = Lists.newArrayList(commentRepository.findAll(byItemId));
        List<Comment> expectedCommentsByItemId = List.of(comment1);

        assertEquals(expectedCommentsByItemId, actualCommentsByItemId);
    }

    @Test
    void calculateCountOfComments() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(JPQLTemplates.DEFAULT, em.getEntityManager());
        QComment qComment = QComment.comment;

        OrderSpecifier<Long> orderById = qComment.id.desc();

        List<Comment> actualCommentsOrderByIdAndLimit2 = queryFactory.selectFrom(qComment)
                .orderBy(orderById)
                .limit(2L)
                .fetch();
        List<Comment> expectedCommentsOrderByIdAndLimit2 = List.of(comment3, comment2);

        assertEquals(expectedCommentsOrderByIdAndLimit2, actualCommentsOrderByIdAndLimit2);
    }
}
