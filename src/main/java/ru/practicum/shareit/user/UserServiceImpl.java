package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.util.CopyUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public List<User> getAllUsers() {
        return repository.findAll().stream().toList();
    }

    @Override
    public User getUser(Long id) {
        return getOrElseThrow(id);
    }

    @Override
    public User saveUser(User user) {
        if (isDuplicateEmail(user))
            throw new DuplicateException("Пользователь с таким Email уже существует", user.getEmail(), "email");
        return repository.save(user);
    }

    @Override
    public User updateUser(User updatedUser, Long userId) {
        updatedUser.setId(userId);
        User oldUser = getOrElseThrow(userId);
        if (isDuplicateEmail(updatedUser))
            throw new DuplicateException("Пользователь с таким Email уже существует", updatedUser.getEmail(), "email");
        CopyUtil.copyNonNullProperties(updatedUser, oldUser);
        return oldUser;
    }

    @Override
    public boolean isUserExists(Long id) {
        return repository.findById(id).isPresent();
    }

    @Override
    public User deleteUser(Long userId) {
        return repository.delete(userId).orElseThrow(
                () -> new NotFoundException("Не найден пользователь с id=" + userId, userId.toString(), "id"));
    }

    private User getOrElseThrow(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new NotFoundException("Не найден пользователь с id=" + id, id.toString(), "id"));
    }

    private boolean isDuplicateEmail(User user) {
        return repository.findAll().stream()
                .map(User::getEmail)
                .anyMatch(email -> email.equalsIgnoreCase(user.getEmail()));
    }

}