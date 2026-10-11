package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.entity.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();

    User getUser(Long id);

    User saveUser(UserDto userDto);

    User updateUser(UserDto updatedUserDto, Long userId);

    boolean isUserExists(Long id);

    void deleteUser(Long userId);
}