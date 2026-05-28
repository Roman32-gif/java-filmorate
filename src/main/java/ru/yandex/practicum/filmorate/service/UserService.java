package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import java.util.*;

@Service
public class UserService {

    private final UserStorage userStorage;
    private static final Logger userLog = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Long userId, Long friendId) {
        userLog.debug("Добавление пользоователя в друзья");
        if (Objects.equals(userId, friendId)) {
            userLog.error("Нельзя добавить в друзья пользователей с одинаоквым id");
            throw new DuplicatedDataException("Нельзя добавить в друзья пользователей с одинаоквым id");
        }

        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        if (user == null || friend == null) {
            userLog.error("Пользователь не найден");
            throw new NotFoundException("Пользователь не найден");
        }

        userLog.info("Добавление пользователя в друзья");
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        userStorage.updateUser(user);
        userStorage.updateUser(friend);
    }

    public void deleteFriend(Long userId, Long friendId) {
        userLog.debug("Удаление пользователя из друзей");
        if (Objects.equals(userId, friendId)) {
            userLog.error("Нельзя удалить из друзей пользователей с одинаоквым id");
            throw new DuplicatedDataException("Нельзя удалить из друзей пользователей с одинаоквым id");
        }

        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        if (user == null || friend == null) {
            userLog.error("Пользователь не найден");
            throw new NotFoundException("Пользователь не найден");
        }

        userLog.info("Удаление пользователя из друзей");
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        userStorage.updateUser(user);
        userStorage.updateUser(friend);
    }

    public List<User> showSameFriends(Long userId, Long friendId) {
        userLog.debug("Вывод одинаковых друзей");

        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        if (user == null || friend == null) {
            userLog.error("Пользователь не найден");
            throw new NotFoundException("Пользователь не найден");
        }

        userLog.info("Вывод общих друзей");
        Set<Long> sameId = new HashSet<>(user.getFriends());
        sameId.retainAll(friend.getFriends());
        List<User> sameFriends = new ArrayList<>();
        for (Long id : sameId) {
            sameFriends.add(userStorage.getUserById(id));
        }
        return sameFriends;
    }

    public List<User> showFriends(Long userId) {
        userLog.debug("Вывод друзей пользователя");

        User user = userStorage.getUserById(userId);

        if (user == null) {
            userLog.error("Пользователь не найден");
            throw new NotFoundException("Пользователь не найден");
        }

        userLog.info("Вывод друзей");
        List<Long> friendsId = new ArrayList<>(user.getFriends());
        List<User> userFriends = new ArrayList<>();
        for (Long id : friendsId) {
            userFriends.add(userStorage.getUserById(id));
        }
        return userFriends;
    }

    public Collection<User> allUsers() {
        return userStorage.allUsers();
    }

    public User createUser(User user) {
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }
}
