package ru.yandex.practicum.filmorate.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private static final Logger filmLog = LoggerFactory.getLogger(InMemoryFilmStorage.class);
    private final Map<Long, Film> filmMap = new HashMap<>();
    private long currentMaxId = 0;
    private static final LocalDate LIMITATION_DAY = LocalDate.of(1895, 12, 28);

    public Collection<Film> allFilms() {
        filmLog.debug("Вывод всех добавленных фильмов");
        return filmMap.values();
    }

    public Film createFilm(Film film) {
        filmLog.debug("Начало добавления нового фильма");
        validate(film);
        film.setId(getNextId());
        filmMap.put(film.getId(), film);
        filmLog.info("Новый фильм успешно добавлен");
        return film;
    }

    public Film getFilmById(Long filmId) {
        filmLog.debug("Получение id фильма");
        if (filmMap.containsKey(filmId)) {
            return filmMap.get(filmId);
        } else {
            filmLog.error("Не удалось найти фтльм с такми id: {}", filmId);
            throw new NotFoundException("Фильма с таким id нет");
        }
    }

    private long getNextId() {
        return ++currentMaxId;
    }

    public Film updateFilm(Film film) {
        filmLog.debug("Начало изменения данных уже существующего фильма");

        if (!filmMap.containsKey(film.getId())) {
            filmLog.error("Ошибка при обновлении данных фильма, фильма с таким id нет: {}", film.getId());
            throw new NotFoundException("Нет фильма с таким id");
        }

        validate(film);
        filmMap.put(film.getId(), film);
        filmLog.info("Данные фильма успешно обновлены");
        return film;
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
            filmLog.warn("Продолжительно фильма не может быть отрицательным числом: {}", film.getDuration());
            throw new ConditionsNotMetException("Продолжительность фильма не может быть отрицательным числом");
        }
    }


}
