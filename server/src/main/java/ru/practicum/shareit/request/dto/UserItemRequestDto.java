package ru.practicum.shareit.request.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.request.Offer;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserItemRequestDto {
    private Long id;
    private String description;
    private LocalDateTime created;
    private List<Offer> items;
}
