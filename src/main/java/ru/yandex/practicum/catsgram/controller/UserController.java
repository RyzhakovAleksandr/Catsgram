package ru.yandex.practicum.catsgram.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DublicatedDataException;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getUsers() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ConditionsNotMetException("Имейл должен быть указан");
        }
        if(isEmailAlreadyUsed(user.getEmail())) {
            throw new DublicatedDataException("Этот имейл уже используется");
        }
        user.setId(getNextId());
        user.setRegistrationDate(Instant.now());

        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User updateUser) {
        if (updateUser.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        User existingUser = users.get(updateUser.getId());
        if (existingUser == null) {
            throw new ConditionsNotMetException(String.format("Пользователь с id %d не найден",  updateUser.getId()));
        }

        if (updateUser.getEmail() != null &&
                !updateUser.getEmail().equals(existingUser.getEmail()) &&
                isEmailAlreadyUsed(updateUser.getEmail())) {
                throw new DublicatedDataException("Этот имейл уже используется");
        }

        updateUserFields(existingUser, updateUser);

        return existingUser;
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
