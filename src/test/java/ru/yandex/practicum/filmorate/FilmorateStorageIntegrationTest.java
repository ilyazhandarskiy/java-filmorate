package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.filmLike.FilmLikeStorage;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class FilmorateStorageIntegrationTest {

    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final FilmLikeStorage filmLikeStorage;
    private final FriendshipStorage friendshipStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Autowired
    FilmorateStorageIntegrationTest(
            @Qualifier("userDbStorage") UserStorage userStorage,
            @Qualifier("filmDbStorage") FilmStorage filmStorage,
            @Qualifier("filmLikeDbStorage") FilmLikeStorage filmLikeStorage,
            @Qualifier("friendshipDbStorage") FriendshipStorage friendshipStorage,
            GenreStorage genreStorage,
            MpaStorage mpaStorage
    ) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.filmLikeStorage = filmLikeStorage;
        this.friendshipStorage = friendshipStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
    }

    private User user1;
    private User user2;
    private User user3;

    private Film film1;
    private Film film2;

    @BeforeEach
    void init() {
        user1 = newUser(
                "first@test.ru",
                "first",
                "User1",
                LocalDate.of(1982, 10, 8)
        );

        user2 = newUser(
                "second@test.ru",
                "second",
                "User2",
                LocalDate.of(1983, 3, 24)
        );

        user3 = newUser(
                "third@test.ru",
                "third",
                "User3",
                LocalDate.of(2006, 12, 25)
        );

        film1 = newFilm(
                "Film1",
                "description Film1",
                LocalDate.of(1989, 4, 14),
                120,
                new Mpa(1L, "G"),
                Set.of(
                        new Genre(1L, "Комедия"),
                        new Genre(2L, "Драма")
                )
        );

        film2 = newFilm(
                "Film2",
                "description Film2",
                LocalDate.of(2015, 5, 30),
                108,
                new Mpa(3L, "PG-13"),
                Set.of(new Genre(6L, "Боевик"))
        );
    }

    @Test
    @DisplayName("Контекст поднимается на in-memory H2, справочники genre/mpa заполнены")
    void contextLoadsWithInMemoryDatabase() {
        assertTrue(
                datasourceUrl.startsWith("jdbc:h2:mem:")
                        || datasourceUrl.contains(":h2:mem:")
        );

        assertThat(genreStorage.getAll()).hasSize(6);
        assertThat(mpaStorage.getAll()).hasSize(5);
    }

    @Test
    @DisplayName("Пользователь создаётся и находится по id")
    void shouldCreateUserAndGetUserById() {
        User created = userStorage.createUser(user1);

        User found = userStorage.getUserById(created.getId());

        assertThat(found)
                .extracting(
                        User::getId,
                        User::getEmail,
                        User::getLogin,
                        User::getName,
                        User::getBirthday
                )
                .containsExactly(
                        created.getId(),
                        "first@test.ru",
                        "first",
                        "User1",
                        LocalDate.of(1982, 10, 8)
                );
    }

    @Test
    @DisplayName("Список пользователей пуст, если ни один не создан")
    void shouldReturnEmptyListWhenNoUsersCreated() {
        assertThat(userStorage.getAllUsers()).isEmpty();
    }

    @Test
    @DisplayName("Возвращаются все созданные пользователи (порядок не важен)")
    void shouldGetAllUsers() {
        User createdUser1 = userStorage.createUser(user1);
        User createdUser2 = userStorage.createUser(user2);

        Collection<User> users = userStorage.getAllUsers();

        assertThat(users)
                .extracting(User::getId)
                .containsExactlyInAnyOrder(
                        createdUser1.getId(),
                        createdUser2.getId()
                );
    }

    @Test
    @DisplayName("Обновление пользователя меняет данные и сохраняется в БД")
    void shouldUpdateUser() {
        User created = userStorage.createUser(user1);

        User userForUpdate = newUser(
                "first@test.ru",
                "first",
                "Updated User1",
                LocalDate.of(1982, 10, 8)
        );
        userForUpdate.setId(created.getId());

        User updated = userStorage.updateUser(userForUpdate);

        assertThat(updated)
                .extracting(
                        User::getId,
                        User::getEmail,
                        User::getLogin,
                        User::getName,
                        User::getBirthday
                )
                .containsExactly(
                        created.getId(),
                        "first@test.ru",
                        "first",
                        "Updated User1",
                        LocalDate.of(1982, 10, 8)
                );

        User storedUser = userStorage.getUserById(created.getId());

        assertThat(storedUser.getName()).isEqualTo("Updated User1");
    }

    @Test
    @DisplayName("Пользователь удаляется и повторное удаление бросает NotFoundException")
    void shouldDeleteUser() {
        User created = userStorage.createUser(user1);

        userStorage.deleteUser(created.getId());

        assertThat(userStorage.getAllUsers()).isEmpty();

        assertThrows(
                NotFoundException.class,
                () -> userStorage.getUserById(created.getId())
        );

        assertThrows(
                NotFoundException.class,
                () -> userStorage.deleteUser(created.getId())
        );
    }

    @Test
    @DisplayName("Получение несуществующего пользователя бросает NotFoundException")
    void shouldThrowWhenUserNotFound() {
        assertThrows(
                NotFoundException.class,
                () -> userStorage.getUserById(999L)
        );
    }

    @Test
    @DisplayName("Фильм создаётся и находится по id вместе с жанрами и MPA")
    void shouldCreateFilmAndGetFilmById() {
        Film created = filmStorage.createFilm(film1);

        Film found = filmStorage.getFilmById(created.getId());

        assertThat(found)
                .extracting(
                        Film::getId,
                        Film::getName,
                        Film::getDescription,
                        Film::getReleaseDate,
                        Film::getDuration
                )
                .containsExactly(
                        created.getId(),
                        "Film1",
                        "description Film1",
                        LocalDate.of(1989, 4, 14),
                        120
                );

        assertThat(found.getMpa())
                .extracting(Mpa::getId, Mpa::getName)
                .containsExactly(1L, "G");

        assertThat(found.getGenres())
                .extracting(Genre::getId)
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("Список фильмов пуст, если ни один не создан")
    void shouldReturnEmptyListWhenNoFilmsCreated() {
        assertThat(filmStorage.getAllFilms()).isEmpty();
    }

    @Test
    @DisplayName("Возвращаются все созданные фильмы (порядок не важен)")
    void shouldGetAllFilms() {
        Film createdFilm1 = filmStorage.createFilm(film1);
        Film createdFilm2 = filmStorage.createFilm(film2);

        Collection<Film> films = filmStorage.getAllFilms();

        assertThat(films)
                .extracting(Film::getId)
                .containsExactlyInAnyOrder(
                        createdFilm1.getId(),
                        createdFilm2.getId()
                );
    }

    @Test
    @DisplayName("Обновление фильма меняет данные, MPA и набор жанров")
    void shouldUpdateFilm() {
        Film created = filmStorage.createFilm(film1);

        Film filmForUpdate = newFilm(
                "Updated Film1",
                "Updated description Film1",
                LocalDate.of(1989, 4, 14),
                125,
                new Mpa(2L, "PG"),
                Set.of(new Genre(6L, "Боевик"))
        );
        filmForUpdate.setId(created.getId());

        Film updated = filmStorage.updateFilm(filmForUpdate);

        assertThat(updated)
                .extracting(
                        Film::getId,
                        Film::getName,
                        Film::getDescription,
                        Film::getReleaseDate,
                        Film::getDuration
                )
                .containsExactly(
                        created.getId(),
                        "Updated Film1",
                        "Updated description Film1",
                        LocalDate.of(1989, 4, 14),
                        125
                );

        assertThat(updated.getMpa())
                .extracting(Mpa::getId, Mpa::getName)
                .containsExactly(2L, "PG");

        assertThat(updated.getGenres())
                .extracting(Genre::getId)
                .containsExactlyInAnyOrder(6L);

        Film found = filmStorage.getFilmById(created.getId());

        assertThat(found.getName()).isEqualTo("Updated Film1");
        assertThat(found.getDescription())
                .isEqualTo("Updated description Film1");
        assertThat(found.getDuration()).isEqualTo(125);
        assertThat(found.getMpa().getId()).isEqualTo(2L);
        assertThat(found.getGenres())
                .extracting(Genre::getId)
                .containsExactlyInAnyOrder(6L);
    }

    @Test
    @DisplayName("Фильм удаляется и повторное удаление бросает NotFoundException")
    void shouldDeleteFilm() {
        Film created = filmStorage.createFilm(film1);

        filmStorage.deleteFilm(created.getId());

        assertThat(filmStorage.getAllFilms()).isEmpty();

        assertThrows(
                NotFoundException.class,
                () -> filmStorage.getFilmById(created.getId())
        );

        assertThrows(
                NotFoundException.class,
                () -> filmStorage.deleteFilm(created.getId())
        );
    }

    @Test
    @DisplayName("Получение несуществующего фильма бросает NotFoundException")
    void shouldThrowWhenFilmNotFound() {
        assertThrows(
                NotFoundException.class,
                () -> filmStorage.getFilmById(999L)
        );
    }

    @Test
    @DisplayName("Справочники жанров и MPA возвращаются полностью и по id")
    void shouldGetAllGenresAndMpa() {
        List<Genre> genres = genreStorage.getAll();
        List<Mpa> mpa = mpaStorage.getAll();

        assertThat(genres)
                .extracting(Genre::getName)
                .containsExactlyInAnyOrder(
                        "Комедия",
                        "Драма",
                        "Мультфильм",
                        "Триллер",
                        "Документальный",
                        "Боевик"
                );

        assertThat(mpa)
                .extracting(Mpa::getName)
                .containsExactlyInAnyOrder(
                        "G",
                        "PG",
                        "PG-13",
                        "R",
                        "NC-17"
                );

        assertThat(genreStorage.getById(1L))
                .extracting(Genre::getId, Genre::getName)
                .containsExactly(1L, "Комедия");

        assertThat(mpaStorage.getById(3L))
                .extracting(Mpa::getId, Mpa::getName)
                .containsExactly(3L, "PG-13");
    }

    @Test
    @DisplayName("Получение несуществующего жанра или MPA бросает NotFoundException")
    void shouldThrowWhenGenreOrMpaNotFound() {
        assertThrows(
                NotFoundException.class,
                () -> genreStorage.getById(999L)
        );

        assertThrows(
                NotFoundException.class,
                () -> mpaStorage.getById(999L)
        );
    }

    @Test
    @DisplayName("Лайк можно поставить и снять без ошибок")
    void shouldAddAndRemoveLike() {
        User createdUser = userStorage.createUser(user1);
        Film createdFilm = filmStorage.createFilm(film1);

        assertThatCode(() -> filmLikeStorage.addLike(
                createdFilm.getId(),
                createdUser.getId()
        )).doesNotThrowAnyException();

        assertThatCode(() -> filmLikeStorage.removeLike(
                createdFilm.getId(),
                createdUser.getId()
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Повторный лайк тем же пользователем не приводит к ошибке и не дублируется")
    void shouldNotFailOnDuplicateLike() {
        User createdUser = userStorage.createUser(user1);
        Film createdFilm = filmStorage.createFilm(film1);

        assertThatCode(() -> {
            filmLikeStorage.addLike(createdFilm.getId(), createdUser.getId());
            filmLikeStorage.addLike(createdFilm.getId(), createdUser.getId());
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Снятие несуществующего лайка не приводит к необработанной ошибке")
    void shouldHandleRemoveLikeWhenLikeDoesNotExist() {
        User createdUser = userStorage.createUser(user1);
        Film createdFilm = filmStorage.createFilm(film1);

        assertThatCode(() -> filmLikeStorage.removeLike(
                createdFilm.getId(),
                createdUser.getId()
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Дружба добавляется только в список инициатора (односторонняя)")
    void shouldAddFriendOnlyToInitiatorList() {
        User createdUser1 = userStorage.createUser(user1);
        User createdUser2 = userStorage.createUser(user2);

        friendshipStorage.addFriend(
                createdUser1.getId(),
                createdUser2.getId()
        );

        List<User> firstUserFriends = friendshipStorage.getFriends(
                createdUser1.getId()
        );

        assertThat(firstUserFriends)
                .extracting(User::getId)
                .containsExactly(createdUser2.getId());

        assertThat(friendshipStorage.getFriends(createdUser2.getId()))
                .isEmpty();
    }

    @Test
    @DisplayName("Взаимная дружба отражается в списках друзей обоих пользователей")
    void shouldSupportMutualFriendship() {
        User createdUser1 = userStorage.createUser(user1);
        User createdUser2 = userStorage.createUser(user2);

        friendshipStorage.addFriend(createdUser1.getId(), createdUser2.getId());
        friendshipStorage.addFriend(createdUser2.getId(), createdUser1.getId());

        assertThat(friendshipStorage.getFriends(createdUser1.getId()))
                .extracting(User::getId)
                .containsExactly(createdUser2.getId());

        assertThat(friendshipStorage.getFriends(createdUser2.getId()))
                .extracting(User::getId)
                .containsExactly(createdUser1.getId());
    }

    @Test
    @DisplayName("Удаление друга убирает его только из списка инициатора")
    void shouldRemoveFriendFromInitiatorList() {
        User createdUser1 = userStorage.createUser(user1);
        User createdUser2 = userStorage.createUser(user2);
        User createdUser3 = userStorage.createUser(user3);

        friendshipStorage.addFriend(
                createdUser1.getId(),
                createdUser2.getId()
        );

        friendshipStorage.addFriend(
                createdUser1.getId(),
                createdUser3.getId()
        );

        friendshipStorage.removeFriend(
                createdUser1.getId(),
                createdUser3.getId()
        );

        List<User> friends = friendshipStorage.getFriends(
                createdUser1.getId()
        );

        assertThat(friends)
                .extracting(User::getId)
                .containsExactly(createdUser2.getId());

        assertThat(friendshipStorage.getFriends(createdUser3.getId()))
                .isEmpty();
    }

    @Test
    @DisplayName("Удаление несуществующей дружбы не приводит к необработанной ошибке")
    void shouldHandleRemoveFriendWhenFriendshipDoesNotExist() {
        User createdUser1 = userStorage.createUser(user1);
        User createdUser2 = userStorage.createUser(user2);

        assertThatCode(() -> friendshipStorage.removeFriend(
                createdUser1.getId(),
                createdUser2.getId()
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Общие друзья находятся корректно при односторонних дружбах")
    void shouldGetCommonFriendsForOneWayFriendships() {
        User createdUser1 = userStorage.createUser(user1);
        User createdUser2 = userStorage.createUser(user2);
        User createdUser3 = userStorage.createUser(user3);

        friendshipStorage.addFriend(
                createdUser1.getId(),
                createdUser3.getId()
        );

        friendshipStorage.addFriend(
                createdUser2.getId(),
                createdUser3.getId()
        );

        List<User> commonFriends = friendshipStorage.getCommonFriends(
                createdUser1.getId(),
                createdUser2.getId()
        );

        assertThat(commonFriends)
                .extracting(User::getId)
                .containsExactly(createdUser3.getId());
    }

    @Test
    @DisplayName("Общие друзья пусты, если общих друзей нет")
    void shouldReturnEmptyCommonFriendsWhenNoneShared() {
        User createdUser1 = userStorage.createUser(user1);
        User createdUser2 = userStorage.createUser(user2);

        assertThat(friendshipStorage.getCommonFriends(
                createdUser1.getId(),
                createdUser2.getId()
        )).isEmpty();
    }

    private static User newUser(
            String email,
            String login,
            String name,
            LocalDate birthday
    ) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(birthday);
        return user;
    }

    private static Film newFilm(
            String name,
            String description,
            LocalDate releaseDate,
            int duration,
            Mpa mpa,
            Set<Genre> genres
    ) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(releaseDate);
        film.setDuration(duration);
        film.setMpa(mpa);
        film.setGenres(new LinkedHashSet<>(genres));
        return film;
    }
}