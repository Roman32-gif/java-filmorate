package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import java.util.Collection;
import java.util.List;
import ru.yandex.practicum.filmorate.service.FilmService;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> allFilms() {
        return filmService.allFilms();
    }

    @PostMapping
    public Film createFilm(@RequestBody Film film) {
        return filmService.createFilm(film);
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @PutMapping ("/{id}/like/{userId}")
    public void addLikeOnVideo(@PathVariable long id, @PathVariable long userId) {
        filmService.addLikeOnVideo(id, userId);
    }

    @DeleteMapping ("/{id}/like/{userId}")
    public void deleteLikeFromVideo(@PathVariable long id, @PathVariable long userId) {
        filmService.deleteLikeFromVideo(id, userId);
    }

    @GetMapping ("/popular")
    public List<Film> show10MostPopularFilms(@RequestParam(defaultValue = "10") int count) {
       return filmService.show10MostPopularFilmsByLikes(count);
    }
}
