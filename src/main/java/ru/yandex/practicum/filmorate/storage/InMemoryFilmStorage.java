package ru.yandex.practicum.filmorate.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private static final Logger filmLog = LoggerFactory.getLogger(InMemoryFilmStorage.class);
    private final Map<Long, Film> filmMap = new HashMap<>();
    private long currentMaxId = 0;

    public Collection<Film> allFilms() {
        filmLog.debug("Вывод всех добавленных фильмов");
        return filmMap.values();
    }

    public Film createFilm(Film film) {
        filmLog.debug("Начало добавления нового фильма");

        film.setId(getNextId());
        filmMap.put(film.getId(), film);
        filmLog.info("Новый фильм успешно добавлен");
        return film;
    }

    public Optional<Film> getFilmById(Long filmId) {
        filmLog.debug("Получение фильма по id: {}", filmId);
        return Optional.ofNullable(filmMap.get(filmId));
    }

    private long getNextId() {
        return ++currentMaxId;
    }

    public Film updateFilm(Film film) {
        filmLog.debug("Начало изменения данных уже существующего фильма");

        filmMap.put(film.getId(), film);
        filmLog.info("Данные фильма успешно обновлены");
        return film;
    }
}
