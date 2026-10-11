package ru.practicum.shareit.request.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.entity.User;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@Entity
@Table(name = "requests")
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    LocalDateTime created;

    public ItemRequest(String description, User owner, LocalDateTime created) {
        this.description = description;
        this.owner = owner;
        this.created = created;
    }
}
