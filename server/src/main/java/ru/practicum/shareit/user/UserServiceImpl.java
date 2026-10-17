package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.entity.User;

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
    public User saveUser(UserDto userDto) {
        if (isDuplicateEmail(userDto))
            throw new DuplicateException("Пользователь с таким Email уже существует", userDto.getEmail(), "email");
        return repository.save(UserMapper.toEntity(userDto));
    }

    @Override
    public User updateUser(UserDto updatedUserDto, Long userId) {
        updatedUserDto.setId(userId);
        User oldUser = getOrElseThrow(userId);
        if (isDuplicateEmail(updatedUserDto))
            throw new DuplicateException("Пользователь с таким Email уже существует", updatedUserDto.getEmail(), "email");
        UserMapper.updateEntity(updatedUserDto, oldUser);
        repository.save(oldUser);
        return oldUser;
    }

    @Override
    public boolean isUserExists(Long id) {
        return repository.findById(id).isPresent();
    }

    @Override
    public void deleteUser(Long userId) {
        User user = repository.findById(userId).orElseThrow(
                () -> new NotFoundException("Не найден пользователь с id=" + userId, userId.toString(), "id"));
        repository.deleteById(user.getId());
    }

    private User getOrElseThrow(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new NotFoundException("Не найден пользователь с id=" + id, id.toString(), "id"));
    }

    private boolean isDuplicateEmail(UserDto userDto) {
        return repository.existsByEmailIgnoreCase(userDto.getEmail());
    }
}
