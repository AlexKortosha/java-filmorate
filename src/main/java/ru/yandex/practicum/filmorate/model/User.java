package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class User {
    private Long id;

    @NotBlank(message = "email должен быть указан")
    @Email(message = "email должен содержать символ '@'")
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "^\\S+$", message = "Логин не может содержать пробелы")
    private String login;

    private String name;

    @NotNull(message = "Дата рождения должна быть указана")
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}
