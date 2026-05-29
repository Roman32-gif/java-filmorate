package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.DuplicatedDataException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserStorage userStorage;
    private static final Logger userLog = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Long userId, Long friendId) {
        userLog.debug("Добавление пользователя в друзья");
        if (Objects.equals(userId, friendId)) {
            userLog.error("Нельзя добавить в друзья пользователей с одинаковым id");
            throw new DuplicatedDataException("Нельзя добавить в друзья пользователей с одинаковым id");
        }

        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        userLog.info("Добавление пользователя в друзья");
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        userStorage.updateUser(user);
        userStorage.updateUser(friend);
    }

    public void deleteFriend(Long userId, Long friendId) {
        userLog.debug("Удаление пользователя из друзей");
        if (Objects.equals(userId, friendId)) {
            userLog.error("Нельзя удалить из друзей пользователей с одинаковым id");
            throw new DuplicatedDataException("Нельзя удалить из друзей пользователей с одинаковым id");
        }

        User user = getUserOrThrow(userId);
        User friend =getUserOrThrow(friendId);

        userLog.info("Удаление пользователя из друзей");
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        userStorage.updateUser(user);
        userStorage.updateUser(friend);
    }

    public List<User> showSameFriends(Long userId, Long friendId) {
        userLog.debug("Вывод одинаковых друзей");

        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);
        userLog.info("Вывод общих друзей");

        return user.getFriends().stream()
                .filter(friend.getFriends()::contains)
                .map(userStorage::getUserById)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    public List<User> showFriends(Long userId) {
        userLog.debug("Вывод друзей пользователя");

        User user = getUserOrThrow(userId);
        userLog.info("Вывод друзей");

        return user.getFriends().stream()
                .map(userStorage::getUserById)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    public Collection<User> allUsers() {
        return userStorage.allUsers();
    }

    public User createUser(User user) {
        validate(user);
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        validate(user);
        getUserOrThrow(user.getId());
        return userStorage.updateUser(user);
    }

    private User getUserOrThrow(Long userId) {
        return userStorage.getUserById(userId)
                .orElseThrow(() -> {
                    userLog.error("Пользователь с id {} не существует", userId);
                    return new ConditionsNotMetException("Такого пользователя нет");
                });
    }

    private void validate(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            userLog.warn("Введён некоррректный email");
            throw new ConditionsNotMetException("Email должен быть обязательно правильно указан");
        }

        if (user.getLogin() == null || user.getLogin().contains(" ") || user.getLogin().isBlank()) {
            userLog.warn("Введён некорректный логин");
            throw new ConditionsNotMetException("Логин должен быть правильно указан");
        }

        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            userLog.warn("Введена некорректная дата рождения");
            throw new ConditionsNotMetException("Дата рождения не может быть в будущем");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
