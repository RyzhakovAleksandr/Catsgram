package ru.yandex.practicum.catsgram.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DublicatedDataException;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    private final Map<Long, User> users = new HashMap<>();

    public Collection<User> getUsers() {
        return users.values();
    }

    public User create(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ConditionsNotMetException("Имейл должен быть указан");
        }
        if (isEmailAlreadyUsed(user.getEmail())) {
            throw new DublicatedDataException("Этот имейл уже используется");
        }
        user.setId(getNextId());
        user.setRegistrationDate(Instant.now());

        users.put(user.getId(), user);
        return user;
    }

    public User update(User updateUser) {
        if (updateUser.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        User existingUser = users.get(updateUser.getId());
        if (existingUser == null) {
            throw new ConditionsNotMetException(String.format("Пользователь с id %d не найден", updateUser.getId()));
        }

        if (updateUser.getEmail() != null &&
                !updateUser.getEmail().equals(existingUser.getEmail()) &&
                isEmailAlreadyUsed(updateUser.getEmail())) {
            throw new DublicatedDataException("Этот имейл уже используется");
        }

        updateUserFields(existingUser, updateUser);

        return existingUser;
    }

    public Optional<User> findUserById(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    private boolean isEmailAlreadyUsed(String email) {
        return users.values()
                .stream()
                .anyMatch(user -> email.equals(user.getEmail()));
    }

    private void updateUserFields(User existingUser, User updateUser) {
        if (updateUser.getEmail() != null) {
            existingUser.setEmail(updateUser.getEmail());
        }
        if (updateUser.getUsername() != null) {
            existingUser.setUsername(updateUser.getUsername());
        }
        if (updateUser.getPassword() != null) {
            existingUser.setPassword(updateUser.getPassword());
        }
    }

    private long getNextId() {
        return users.keySet()
                .stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L) + 1;
    }
}
