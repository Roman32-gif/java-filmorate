package ru.yandex.practicum.filmorate.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.DuplicatedDataException;
import ru.yandex.practicum.filmorate.model.User;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryUserStorage implements UserStorage {

    private static final Logger userLog = LoggerFactory.getLogger(InMemoryUserStorage.class);
    private final Map<Long, User> userMap = new HashMap<>();
    private long currentMaxId = 0;

    public Collection<User> allUsers() {
        userLog.debug("Вывод всех созданных пользователей");
        return userMap.values();
    }

    public Optional<User> getUserById(Long userId) {
        userLog.debug("Проверка на наличие такого пользователя");
        return Optional.ofNullable(userMap.get(userId));
    }

    public void deleteFriendById(Long friendId) {
        userLog.debug("Удаление друга");
        userMap.remove(friendId);
    }

    public User createUser(User user) {
        userLog.debug("Начало создания нового пользователя");
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

        checkTheSameEmail(user);
        userLog.info("Обновление данных существующего пользователя");
        userMap.put(user.getId(), user);
        return user;
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
