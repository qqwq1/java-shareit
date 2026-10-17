package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CommonItemRequestDto {
    private Long id;
    private String description;
    private LocalDateTime created;
}
