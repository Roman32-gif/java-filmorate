package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.DuplicatedDataException;
import ru.yandex.practicum.filmorate.model.User;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTests {
    private static UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    @DisplayName("Ввод всех валидных данных: успешное создание нового пользователя")
    @Test
    public void createUserWithValidData() {
        User user = User.builder()
                .email("Creative@yandex.ru")
                .login("bro")
                .name("Ivan")
                .birthday(LocalDate.of(2001, 10, 5))
                .build();

        User newUser = userController.createUser(user);
        assertNotNull(newUser.getId());
    }

 @DisplayName("Ввод всех полей кроме почты: ошибка при создании пользователя")
 @Test
 public void createUserWithoutEmail() {
        User user = User.builder()
                .email("")
                .login("bro")
                .name("Ivan")
                .birthday(LocalDate.of(2001, 10, 5))
                .build();
        assertThrows(ConditionsNotMetException.class, () -> userController.createUser(user), "Новый пользователь не может быть создан без почты");
 }

 @DisplayName("Ввод поля почты без знака @: ошибка при создании нового пользователя")
 @Test
 public void createNewUserWithInvalidEmail() {
     User user = User.builder()
             .email("Creativeyandex.ru")
             .login("bro")
             .name("Ivan")
             .birthday(LocalDate.of(2001, 10, 5))
             .build();
     assertThrows(ConditionsNotMetException.class, () -> userController.createUser(user), "Email должен быть со занокм @");
 }

 @DisplayName("Ввод всех полей кроме логина: ошибка при создании нового пользователя")
 @Test
    public void createUserWithoutLogin() {
     User user = User.builder()
             .email("Creative@yandex.ru")
             .login("")
             .name("Ivan")
             .birthday(LocalDate.of(2001, 10, 5))
             .build();
     assertThrows(ConditionsNotMetException.class, () -> userController.createUser(user), "Поле login не может быть пустым");
 }

 @DisplayName("Ввод поля логин с пробелом: ошибка при создании новго пользователя")
    @Test
    public void createUserWithInvalidLogin() {
     User user = User.builder()
             .email("Creative@yandex.ru")
             .login("big boss")
             .name("Ivan")
             .birthday(LocalDate.of(2001, 10, 5))
             .build();
     assertThrows(ConditionsNotMetException.class, () -> userController.createUser(user), "Поле логин не может быть с пробелом");
 }

 @DisplayName("Воод пустого поля имени - автоматичекое заполнение поля данным из логина: успешное создание поользователя")
    @Test
    public void createUserWithEmptyName() {
     User user = User.builder()
             .email("Creative@yandex.ru")
             .login("bigBoss")
             .name("")
             .birthday(LocalDate.of(2001, 10, 5))
             .build();
     User newUser = userController.createUser(user);
     assertEquals(newUser.getName(), user.getLogin());
 }

 @DisplayName("Ввод даты рождения в будущем: ошибка при создании пользователя")
    @Test
    public void createUserWithInvalidDateOfBirth() {
     User user = User.builder()
             .email("Creative@yandex.ru")
             .login("bigBoss")
             .name("")
             .birthday(LocalDate.now().plusMonths(2))
             .build();
     assertThrows(ConditionsNotMetException.class, () -> userController.createUser(user), "Дата рождения пользователя не может быть в будущем");
 }

 @DisplayName("Ввод всех валидных данных: успешное обновление данных пользователя")
    @Test
    public void updateUser() {
     User user = User.builder()
             .email("Creative@yandex.ru")
             .login("bro")
             .name("Ivan")
             .birthday(LocalDate.of(2001, 10, 5))
             .build();

     User newUser = userController.createUser(user);
     newUser.setName("Vasya");
     User updatedUser = userController.updateUser(newUser);
     assertEquals("Vasya", updatedUser.getName());
     assertEquals(newUser.getId(), updatedUser.getId());
 }

 @DisplayName("Ввод уже существующей почты: ошибка при обновлении данных пользоователя")
    @Test
    public void updateUserWithExistingEmail() {
     User user1 = User.builder()
             .email("Creative@yandex.ru")
             .login("bro")
             .name("Ivan")
             .birthday(LocalDate.of(2001, 10, 5))
             .build();
     userController.createUser(user1);

     User user2 = User.builder()
             .email("Right@yandex.ru")
             .login("bro")
             .name("Ivan")
             .birthday(LocalDate.of(2001, 10, 5))
             .build();
     User newUser2 = userController.createUser(user2);
     newUser2.setEmail("Creative@yandex.ru");
     assertThrows(DuplicatedDataException.class, () -> userController.updateUser(newUser2), "Такая почта уже существует");
 }
}
