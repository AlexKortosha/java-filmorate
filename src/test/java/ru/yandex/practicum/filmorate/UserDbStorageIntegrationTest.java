package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.database.UserDbStorage;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageIntegrationTest {

    private final UserDbStorage userStorage;

    @Test
    void testAddAndFindUserById() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testLogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User savedUser = userStorage.add(user);

        Optional<User> userOptional = userStorage.findUserById(savedUser.getId().intValue());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u.getId()).isEqualTo(savedUser.getId());
                    assertThat(u.getEmail()).isEqualTo("test@example.com");
                    assertThat(u.getLogin()).isEqualTo("testLogin");
                    assertThat(u.getName()).isEqualTo("Test User");
                    assertThat(u.getBirthday()).isEqualTo(LocalDate.of(2000, 1, 1));
                    assertThat(u.getFriends()).isEmpty();
                });
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        user.setEmail("update@example.com");
        user.setLogin("updateLogin");
        user.setName("Update User");
        user.setBirthday(LocalDate.of(1990, 5, 5));

        User savedUser = userStorage.add(user);

        savedUser.setName("Updated Name");
        savedUser.setLogin("updatedLogin");
        userStorage.update(savedUser);

        User updatedUser = userStorage.getById(savedUser.getId());
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getLogin()).isEqualTo("updatedLogin");
    }
}