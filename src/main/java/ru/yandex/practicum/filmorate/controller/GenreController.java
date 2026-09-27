package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;

@RestController
@RequestMapping("/genres")
public class GenreController {
    private final GenreStorage genreStorage;

    public GenreController(GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    @GetMapping
    public Collection<Genre> getGenres() {
        return genreStorage.getAllGenres();
    }

    @GetMapping("/{id}")
    public Genre getGenreById(@PathVariable Long id) {
        return genreStorage.getGenreById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Жанр с id " + id + " не найден"
                ));
    }
}
