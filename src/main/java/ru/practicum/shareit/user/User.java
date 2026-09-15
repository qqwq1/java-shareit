package ru.practicum.shareit.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import ru.practicum.shareit.validation.Create;
import ru.practicum.shareit.validation.Update;


@Data
public class User {
    private Long id;

    @Email(groups = {Create.class, Update.class},
            message = "Некорректный адрес электронной почты")
    @NotBlank(groups = {Create.class},
            message = "Адрес электронной почты не может быть пустым")
    private String email;

    @NotBlank(groups = {Create.class},
            message = "Имя пользователя не может быть пустым")
    private String name;
}