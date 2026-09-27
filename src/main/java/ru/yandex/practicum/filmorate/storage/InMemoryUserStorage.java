package ru.yandex.practicum.filmorate.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component("InMemoryUserStorage")
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

    @Override
    public void addFriend(Long userId, Long friendId) {

    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {

    }

    public void deleteFriendById(Long friendId) {
        userLog.debug("Удаление друга");
        userMap.remove(friendId);
    }

    public User createUser(User user) {
        userLog.debug("Начало создания нового пользователя");
        user.setId(getNextId());
        userMap.put(user.getId(), user);
        userLog.info("Успешное создание нового пользователя: {}", user.getId());
        return user;
    }

    private long getNextId() {
        return ++currentMaxId;
    }

    public User updateUser(User user) {
        userLog.debug("Начало изменения данных существующего пользователя");
        userLog.info("Обновление данных существующего пользователя");
        userMap.put(user.getId(), user);
        return user;
    }
}
