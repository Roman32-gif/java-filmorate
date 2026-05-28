package ru.yandex.practicum.filmorate.model;

import lombok.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@EqualsAndHashCode(of = "id")
public class Film {

    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;

    @Builder.Default
    private  Set<Long> likes = new HashSet<>();

    public Set<Long> getLikes() {
        if (this.likes == null) {
            this.likes = new HashSet<>();
        }
        return this.likes;
    }
}
