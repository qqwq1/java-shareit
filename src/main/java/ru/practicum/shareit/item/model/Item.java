package ru.practicum.shareit.item.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Item {
    private Long id;
    private Long ownerId;
    private String name;
    private String description;
    private Boolean available;
    private List<String> reviews = new ArrayList<>();
}