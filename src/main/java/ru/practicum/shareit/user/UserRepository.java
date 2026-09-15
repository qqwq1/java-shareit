package ru.practicum.shareit.user;

import java.util.Collection;
import java.util.Optional;

interface UserRepository {
    Collection<User> findAll();

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> delete(Long id);
}