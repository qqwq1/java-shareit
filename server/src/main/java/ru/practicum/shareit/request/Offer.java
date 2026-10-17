package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Offer {
    private Long id;
    private String name;
    private Long ownerId;
}
