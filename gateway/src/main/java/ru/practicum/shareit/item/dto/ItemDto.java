package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.handler.Create;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    @NotBlank(groups = {Create.class},
            message = "Наименование вещи не может быть пустым")
    @NotNull(groups = {Create.class},
            message = "Наименование вещи не может быть пустым")
    private String name;

    @NotNull(groups = {Create.class},
            message = "Необходимо указать доступность вещи при создании")
    private Boolean available;

    @NotBlank(groups = {Create.class},
            message = "Описание вещи не может быть пустым")
    @NotNull(groups = {Create.class},
            message = "Описание вещи не может быть пустым")
    private String description;

    private Long requestId;
}
