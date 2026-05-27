package ru.yandex.practicum.filmorate.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryUserStorage implements UserStorage {

    private static final Logger userLog = LoggerFactory.getLogger(InMemoryUserStorage.class);
    private final Map<Long, User> userMap = new HashMap<>();
    private long currentMaxId = 0;

    public Collection<User> allUsers() {
        userLog.debug("Вывод всех созданных пользователей");
        return userMap.values();
    }

    public User getUserById(Long userId) {
        userLog.debug("Проверка на наличие такого пользователя");
        if (userMap.containsKey(userId)) {
            return userMap.get(userId);
        } else {
            userLog.error("Не удалось получить данные пользователя, пользователь с данным id не существует: {}", userId);
            throw new NotFoundException("Нет такого пользователя с таким id");
        }
    }

    public void deleteFriendById(Long friendId) {
        userLog.debug("Удаление друга");
        userMap.remove(friendId);
    }

    public User createUser(User user) {
        userLog.debug("Начало создания нового пользователя");
        validate(user);
        checkTheSameEmail(user);
        user.setId(getNextId());
        userMap.put(user.getId(), user);
        userLog.info("Успешное создание нового пользователя: {}", user.getId());
        return user;
    }

    private long getNextId() {
        return ++currentMaxId;
    }

    public User updateUser(User user) {
        userLog.debug("Начало измененения данных существующего пользователя");

        if (!userMap.containsKey(user.getId())) {
            userLog.error("Не получилось обновить данные пользователя, пользователь с данным id не существует: {}", user.getId());
            throw new NotFoundException("Нет такого пользователя с таким id");
        }

        validate(user);
        checkTheSameEmail(user);
        userLog.info("Обновление данных существующего пользователя");
        userMap.put(user.getId(), user);
        return user;
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

    private void checkTheSameEmail(User user) {
        for (User foundUser : userMap.values()) {
            if (user.getEmail().equals(foundUser.getEmail())) {
                if (!foundUser.getId().equals(user.getId())) {
                    userLog.error("Не получилось обновить данные пользователя, пользователь с данной почтой уже существует: {}", user.getEmail());
                    throw new DuplicatedDataException("Пользователь с такой почтой уже существует");
                }
            }
        }
    }
}
