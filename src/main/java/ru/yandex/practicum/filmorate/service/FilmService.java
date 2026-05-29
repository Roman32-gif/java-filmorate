package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private static final Logger filmLog = LoggerFactory.getLogger(FilmService.class);
    private static final LocalDate LIMITATION_DAY = LocalDate.of(1895, 12, 28);

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public void addLikeOnVideo(Long filmId, Long userId) {
        filmLog.debug("Добавление лайка");

        Film film = getFilmOrThrow(filmId);
        getUserOrThrow(userId);
        filmLog.info("Добавление лайка");
        film.getLikes().add(userId);
    }

    public void deleteLikeFromVideo(Long filmId, Long userId) {
        filmLog.debug("Удаление лайка с видео");

        Film film = getFilmOrThrow(filmId);
        getUserOrThrow(userId);
        filmLog.info("Удаление лайка с видео");
        film.getLikes().remove(userId);
    }

    public List<Film> show10MostPopularFilmsByLikes(int count) {
        filmLog.debug("Вывод {} самых популярных фильмов по лайкам", count);

        if (count <= 0) {
            filmLog.error("Количество фильмов должно быть больше нуля: {}", count);
            throw new ConditionsNotMetException("Количество фильмов должно быть положительным числом");
        }

        return filmStorage.allFilms().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    public Collection<Film> allFilms() {
        return filmStorage.allFilms();
    }

    public Film createFilm(Film film) {
        validate(film);
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        validate(film);
        getFilmOrThrow(film.getId());
        return filmStorage.updateFilm(film);
    }

    private User getUserOrThrow(Long userId) {
        return userStorage.getUserById(userId)
                .orElseThrow(() -> {
                    filmLog.error("Пользователь с id {} не существует", userId);
                    return new NotFoundException("Такого пользователя нет");
                });
    }

    private Film getFilmOrThrow(Long filmId) {
        return filmStorage.getFilmById(filmId)
                .orElseThrow(() -> {
                    filmLog.error("Фильм с id {} не существует", filmId);
                    return new NotFoundException("Фильма с таким id нет");
                });
    }

    private void validate(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            filmLog.warn("Введено пустое имя фильма");
            throw new ConditionsNotMetException("Название не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            filmLog.warn("Описание фильма слишком длинное");
            throw new ConditionsNotMetException("Описание не может быть больше 200 символов");
        }

        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(LIMITATION_DAY)) {
            filmLog.warn("Указана некорректная дата релиза фильма: {}", film.getReleaseDate());
            throw new ConditionsNotMetException("Дата релиза фильма не может быть раньше 28.12.1895");
        }

        if (film.getDuration() <= 0) {
            filmLog.warn("Продолжительность фильма не может быть отрицательным числом: {}", film.getDuration());
            throw new ConditionsNotMetException("Продолжительность фильма не может быть отрицательным числом");
        }
    }
}
