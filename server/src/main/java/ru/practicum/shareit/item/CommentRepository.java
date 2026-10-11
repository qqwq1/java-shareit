package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.entity.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("""
            select c
            from Comment c
            join fetch c.author
            where c.item.id = :itemId
            order by c.created
            """)
    List<Comment> findAllByItem_IdOrderByCreatedAsc(@Param("itemId") Long itemId);

    @Query("""
            select c
            from Comment c
            join fetch c.author
            join fetch c.item i
            where i.id in :itemIds
            order by i.id, c.created
            """)
    List<Comment> findAllByItemIds(@Param("itemIds") List<Long> itemIds);
}
