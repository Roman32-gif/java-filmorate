package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.exceptions.*;

import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTests {
    private static FilmController filmController;

    @BeforeEach
    public void setUp() {
        filmController = new FilmController();
    }

    @DisplayName("Ввод всех валдиных полей: успешное добавление нового фильма")
    @Test
    public void createNewFilmWithValidData() {
        Film film = Film.builder()
                .name("First")
                .description("interesting one")
                .releaseDate(LocalDate.of(2000, 12, 3))
                .duration(120)
                .build();
        Film newFilm = filmController.createFilm(film);
        assertNotNull(newFilm.getId());
    }

    @DisplayName("Поле имя пустое: ошибка при добавлении фильма")
    @Test
    public void addNewFilmWithInvalidName() {
        Film film = Film.builder()
                .name("")
                .description("interesting one")
                .releaseDate(LocalDate.of(2000, 12, 3))
                .duration(120)
                .build();
        assertThrows(ConditionsNotMetException.class, () -> filmController.createFilm(film), "Поле имя не может быть пустым");
    }

    @DisplayName("Длина описания больше 200 символов: ошибка при добавлении фильма")
    @Test
    public void addNewFilmWithInvalidDescription() {
        String description = "a".repeat(201);
        Film film = Film.builder()
                .name("First")
                .description(description)
                .releaseDate(LocalDate.of(2000, 12, 3))
                .duration(120)
                .build();
        assertThrows(ConditionsNotMetException.class, () -> filmController.createFilm(film), "В описании не может быть большее 200 символов");
    }

    @DisplayName("Дата релиза указана раньше 28 декабря 1895 года: ошибка при добавлении фильма")
    @Test
    public void addFNewFilmWithInvalidReleaseDate() {
        Film film = Film.builder()
                .name("First")
                .description("interesting one")
                .releaseDate(LocalDate.of(1895, 12, 27))
                .duration(120)
                .build();
        assertThrows(ConditionsNotMetException.class, () -> filmController.createFilm(film), "Дата релиза фильма не может быть раньше 28 декабря 1895 года");
    }

    @DisplayName("Продолжительность фильма отрицательно число: ошибка при добавлении фильма")
    @Test
    public void addNewFilmWithInvalidDuration() {
        Film film = Film.builder()
                .name("First")
                .description("interesting one")
                .releaseDate(LocalDate.of(2000, 12, 3))
                .duration(-120)
                .build();
        assertThrows(ConditionsNotMetException.class, () -> filmController.createFilm(film), "Продолжительность фильма не может быть отрицательным числом");
    }

    @DisplayName("Ввод всех валидных данных: успешное обновление данных фильма")
    @Test
    public void updateFilmData() {
        Film film = Film.builder()
                .name("First")
                .description("interesting one")
                .releaseDate(LocalDate.of(2000, 12, 3))
                .duration(120)
                .build();
        Film newFilm = filmController.createFilm(film);
        newFilm.setName("Second");
        Film updatedFilm = filmController.updateFilm(newFilm);
        assertEquals("Second", updatedFilm.getName());
        assertEquals(newFilm.getId(), updatedFilm.getId());
    }

    @DisplayName("Обновить данные фильма с несущетсвующим id: ошибка при обновлении данных фильма")
    @Test
    public void updateFilmWithInvalidId() {
        Film film = Film.builder()
                .id(100L)
                .name("First")
                .description("interesting one")
                .releaseDate(LocalDate.of(2000, 12, 3))
                .duration(120)
                .build();
        assertThrows(NotFoundException.class, () -> filmController.updateFilm(film), "Фильма с таким id не существует");
    }
}
