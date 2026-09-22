package ru.practicum.shareit.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.practicum.shareit.item.entity.Item;

import java.util.List;


@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String name;

    @OneToMany(mappedBy = "owner")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Item> items;

}