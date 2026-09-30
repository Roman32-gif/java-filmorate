package ru.yandex.practicum.filmorate.model;

import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
public class Film {

    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    private Mpa mpa;

    @Builder.Default
    private Set<Long> likes = new HashSet<>();

    @Builder.Default
    private List<Genre> genres = new ArrayList<>();

    public Set<Long> getLikes() {
        if (this.likes == null) {
            this.likes = new HashSet<>();
        }
        return this.likes;
    }

    public List<Genre> getGenres() {
        if (this.genres == null) {
            this.genres = new ArrayList<>();
        }
        return this.genres;
    }
}
