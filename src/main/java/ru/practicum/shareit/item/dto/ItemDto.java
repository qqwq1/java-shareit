package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;
import ru.practicum.shareit.validation.Create;
import ru.practicum.shareit.validation.Update;

import java.util.List;

@Data
public class ItemDto {
    private Long id;
    @NotBlank(groups = {Create.class},
            message = "Наименование вещи не может быть пустым")
    private String name;

    @NotNull(groups = {Create.class},
            message = "Неоходимо указать доступность вещи при создании")
    private Boolean available;

    @NotBlank(groups = {Create.class},
            message = "Описание вещи не может быть пустым")
    private String description;

    @Null(groups = {Create.class, Update.class},
            message = "Нельзя добавлять отзывы на вещь при создании")
    private List<String> reviews;


}
