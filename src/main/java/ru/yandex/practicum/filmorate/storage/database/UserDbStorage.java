package ru.yandex.practicum.filmorate.storage.database;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Component("UserDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sql, this::mapRowToUser);
        Map<Long, Set<Long>> allFriends = getAllFriends();
        for (User user : users) {
            user.setFriends(allFriends.getOrDefault(user.getId(), new HashSet<>()));
        }

        return users;
    }

    @Override
    public  User add(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, user.getEmail());
            preparedStatement.setString(2, user.getLogin());
            preparedStatement.setString(3, user.getName());
            preparedStatement.setObject(4, user.getBirthday());
            return preparedStatement;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        log.info("Добавлен пользователь с id={}", user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";

        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());

        return user;
    }

    @Override
    public User getById(Long id) {
        String sql = "SELECT * FROM users WHERE user_id=?";
        List<User> users = jdbcTemplate.query(sql, this::mapRowToUser, id);
        if (users.isEmpty()) {
            return null;
        }
        User user = users.get(0);
        user.setFriends(getUserFriends(user.getId()));
        return user;
    }

    @Override
    public Optional<User> findUserById(int id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        List<User> users = jdbcTemplate.query(sql, this::mapRowToUser, id);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    @Override
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sql, this::mapRowToUser);
        Map<Long, Set<Long>> allFriends = getAllFriends();
        for (User user : users) {
            user.setFriends(allFriends.getOrDefault(user.getId(), new HashSet<>()));
        }

        return users;
    }


    @Override
    public void addFriend(Long userId, Long friendId) {
        String sql = "INSERT INTO friendship (user_id, friend_id) VALUES (?, ?)";
        try {
            jdbcTemplate.update(sql, userId, friendId);
        } catch (Exception e) {
        }
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public Set<Long> getUserFriends(Long userId) {
        String sql = "SELECT friend_id FROM friendship WHERE user_id = ?";
        List<Long> friends = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("friend_id"), userId);
        return new HashSet<>(friends);
    }

    @Override
    public List<User> getCommonFriends(Long userId1, Long userId2) {
        // Получаем id общих друзей через
        String sql = "SELECT f1.friend_id " +
                "FROM friendship f1 " +
                "INNER JOIN friendship f2 ON f1.friend_id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?";
        List<Long> commonFriendIds = jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getLong("friend_id"), userId1, userId2);

        if (commonFriendIds.isEmpty()) {
            return new ArrayList<>();
        }

        String inSql = String.join(",", Collections.nCopies(commonFriendIds.size(), "?"));
        String userSql = "SELECT * FROM users WHERE user_id IN (" + inSql + ")";
        List<User> users = jdbcTemplate.query(userSql, this::mapRowToUser, commonFriendIds.toArray());
        return users;
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        user.setFriends(new HashSet<>());
        user.setFriends(getUserFriends(user.getId()));
        return user;
    }

    private Map<Long, Set<Long>> getAllFriends() {
        String sql = "SELECT user_id, friend_id FROM friendship";
        Map<Long, Set<Long>> allFriends = new HashMap<>();

        jdbcTemplate.query(sql, (rs, rowNum) -> {
            Long userId = rs.getLong("user_id");
            Long friendId = rs.getLong("friend_id");
            allFriends.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
            return null;
        });

        return allFriends;
    }

}
