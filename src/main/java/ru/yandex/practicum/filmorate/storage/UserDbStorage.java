package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

@Repository("UserDbStorage")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<User> allUsers() {
        return jdbcTemplate.query("SELECT * FROM users", (rs, rowNum) -> mappingUser(rs));
    }

    @Override
    public User createUser(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().longValue());
        return user;
    }

    @Override
    public User updateUser(User user) {

         jdbcTemplate.update("UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?",
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());
        return user;
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return jdbcTemplate.query("SELECT * FROM users WHERE id = ?", (rs, rowNum) -> mappingUser(rs), userId)
                .stream()
                .findFirst();
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        jdbcTemplate.update("INSERT INTO user_friends (user_id, friend_id) VALUES (?, ?)", userId, friendId);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        jdbcTemplate.update("DELETE FROM user_friends WHERE user_id = ? AND friend_id = ?", userId, friendId);
    }


    private User mappingUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());

        Set<Long> friends = new HashSet<>();

        jdbcTemplate.query(
                "SELECT friend_id FROM user_friends WHERE user_id = ?",
                (rs1, rowNum) -> {
                    friends.add(rs1.getLong("friend_id"));
                    return null;
                },
                user.getId()
        );

        user.setFriends(friends);

        return user;
    }
}
