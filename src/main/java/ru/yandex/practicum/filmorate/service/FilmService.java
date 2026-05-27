package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private static final Logger filmLog = LoggerFactory.getLogger(FilmService.class);

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public void addLikeOnVideo(Long filmId, Long userId) {
        filmLog.debug("Добавление лайка");

        User user = userStorage.getUserById(userId);
        Film film = filmStorage.getFilmById(filmId);

        if (user == null || film == null) {
            filmLog.error("Такого пользователся или фильма не существует");
            throw new NotFoundException("Такого пользователя или фильма не существует");
        }

        filmLog.info("Добавление лайка");
        film.getLikes().add(userId);
    }

    public void deleteLikeFromVideo(Long filmId, Long userId) {
        filmLog.debug("Удаление лайка с видео");

        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);

        if (user == null || film == null) {
            filmLog.error("Такого пользователся или фильма не существует");
            throw new NotFoundException("Такого пользователя или фильма не существует");
        }

        filmLog.info("Удаление лайка с видео");
        film.getLikes().remove(userId);
    }

    public List<Film> show10MostPopularFilmsByLikes(int count) {
        filmLog.debug("Вывод {} самых популярных фильмов по лайкам", count);

        return filmStorage.allFilms().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    public Collection<Film> allFilms() {
        return filmStorage.allFilms();
    }

    public Film createFilm(Film film) {
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }
}
