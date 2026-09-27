package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, FilmDbStorage.class})
class FilmorateApplicationTests {
	private final UserDbStorage userDbStorage;
	private final FilmDbStorage filmDbStorage;

	@Test
	public void createUser() {
		User user = new User();
		user.setLogin("test1");
		user.setEmail("test1@yandex.ru");
		user.setName("test");
		user.setBirthday(LocalDate.of(1990, 1, 20));
		userDbStorage.createUser(user);
		Collection<User> users = userDbStorage.allUsers();

		assertThat(users)
				.anySatisfy(savedUser -> {
					assertThat(savedUser.getLogin()).isEqualTo("test1");
					assertThat(savedUser.getEmail()).isEqualTo("test1@yandex.ru");
					assertThat(savedUser.getName()).isEqualTo("test");
					assertThat(savedUser.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 20));
				});
	}


	@Test
	public void getUserById() {
		User user = new User();
		user.setLogin("test1");
		user.setEmail("test1@yandex.ru");
		user.setName("test");
		user.setBirthday(LocalDate.of(1990, 1, 20));
		User createdUser = userDbStorage.createUser(user);

		Optional<User> userOptional = userDbStorage.getUserById(createdUser.getId());

		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(newUser -> {
					assertThat(newUser.getId()).isEqualTo(createdUser.getId());
					assertThat(newUser.getLogin()).isEqualTo("test1");
				});
	}

	@Test
	public void getAllUsers() {
		User user = new User();
		user.setLogin("test1");
		user.setEmail("test1@yandex.ru");
		user.setName("test");
		user.setBirthday(LocalDate.of(2000, 1, 20));

		User user2 = new User();
		user2.setLogin("test2");
		user2.setEmail("test1@yandex.ru");
		user2.setName("test");
		user2.setBirthday(LocalDate.of(2000, 1, 20));
		userDbStorage.createUser(user);
		userDbStorage.createUser(user2);

		Collection<User> allUsers = userDbStorage.allUsers();

		assertThat(allUsers)
				.hasSize(2)
				.extracting(User::getLogin)
				.containsExactlyInAnyOrder("test1", "test2");
	}

	@Test
	public void updateUser() {
		User user = new User();
		user.setLogin("test2");
		user.setEmail("test1@yandex.ru");
		user.setName("test");
		user.setBirthday(LocalDate.of(2000, 1, 20));
		User createdUser = userDbStorage.createUser(user);
		createdUser.setLogin("updatedLogin");
		createdUser.setEmail("updated@yandex.ru");
		userDbStorage.updateUser(createdUser);
		Optional<User> updatedUserOptional = userDbStorage.getUserById(createdUser.getId());
		assertThat(updatedUserOptional)
				.isPresent()
				.hasValueSatisfying(foundUser -> {
					assertThat(foundUser.getId()).isEqualTo(createdUser.getId());
					assertThat(foundUser.getLogin()).isEqualTo("updatedLogin");
					assertThat(foundUser.getEmail()).isEqualTo("updated@yandex.ru");
				});
	}

	@Test
	public void createFilm() {
		Film film = new Film();
		film.setName("test1");
		film.setDescription("test");
		film.setReleaseDate(LocalDate.of(1990, 1, 20));
		film.setDuration(120);
		film.setMpaId(1L);
		filmDbStorage.createFilm(film);
		Collection<Film> films = filmDbStorage.allFilms();
		assertThat(films)
				.anySatisfy(savedFilm -> {
					assertThat(savedFilm.getName()).isEqualTo("test1");
					assertThat(savedFilm.getDescription()).isEqualTo("test");
					assertThat(savedFilm.getReleaseDate()).isEqualTo(LocalDate.of(1990, 1, 20));
					assertThat(savedFilm.getDuration()).isEqualTo(120);
				});
	}

	@Test
	public void getFilmById() {
		Film film = new Film();
		film.setName("test1");
		film.setDescription("test");
		film.setReleaseDate(LocalDate.of(1990, 1, 20));
		film.setDuration(120);
		film.setMpaId(1L);
		filmDbStorage.createFilm(film);

		Optional<Film> filmOptional = filmDbStorage.getFilmById(film.getId());

		assertThat(filmOptional)
				.isPresent()
				.hasValueSatisfying(newFilm -> {
					assertThat(newFilm.getName()).isEqualTo(film.getName());
					assertThat(newFilm.getDescription()).isEqualTo(film.getDescription());
				});
	}

	@Test
	public void getAllFilms() {
		Film film = new Film();
		film.setName("test1");
		film.setDescription("test");
		film.setReleaseDate(LocalDate.of(1990, 1, 20));
		film.setDuration(120);
		film.setMpaId(1L);
		Film film2 = new Film();
		film2.setName("test2");
		film2.setDescription("test2");
		film2.setReleaseDate(LocalDate.of(2000, 1, 20));
		film2.setDuration(150);
		film2.setMpaId(1L);

		filmDbStorage.createFilm(film);
		filmDbStorage.createFilm(film2);

		Collection<Film> allFilms = filmDbStorage.allFilms();

		assertThat(allFilms)
				.hasSize(2)
				.extracting(Film::getName)
				.containsExactlyInAnyOrder("test1", "test2");
	}

	@Test
	public void updateFilm() {
		Film film = new Film();
		film.setName("test1");
		film.setDescription("test");
		film.setReleaseDate(LocalDate.of(1990, 1, 20));
		film.setDuration(120);
		film.setMpaId(1L);
		Film newFilm = filmDbStorage.createFilm(film);
		newFilm.setName("test2");
		newFilm.setDescription("test2");
		filmDbStorage.updateFilm(newFilm);

		Optional<Film> filmOptional = filmDbStorage.getFilmById(newFilm.getId());
		assertThat(filmOptional)
				.isPresent()
				.hasValueSatisfying(foundFilm -> {
					assertThat(foundFilm.getName()).isEqualTo("test2");
					assertThat(foundFilm.getDescription()).isEqualTo("test2");
				});
	}
}
