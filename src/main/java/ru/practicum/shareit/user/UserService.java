package ru.practicum.shareit.user;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();

    User getUser(Long id);

    User saveUser(User user);

    User updateUser(User updatedUser, Long userId);

    boolean isUserExists(Long id);

    User deleteUser(Long userId);
}