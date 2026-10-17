package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.entity.Comment;

public final class CommentMapper {
    private CommentMapper() {
    }

    public static CommentResponseDto toDto(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }
}
