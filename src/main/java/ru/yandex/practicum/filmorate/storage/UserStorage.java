package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;
import java.util.Collection;

public interface UserStorage {

    Collection<User> allUsers();
    User createUser(User user);
    User updateUser(User user);
    User getUserById(Long userId);
}
