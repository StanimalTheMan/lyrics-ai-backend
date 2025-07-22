package com.jiggycode.repository;

import com.jiggycode.entity.Song;
import com.jiggycode.entity.User;
import com.jiggycode.entity.UserSong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserSongRepository extends JpaRepository<UserSong, Long> {
    boolean existsByUserAndSong(User user, Song song);
    @Query("""
        SELECT us FROM UserSong us
        JOIN FETCH us.song
           WHERE us.user = :user
    """)
    List<UserSong> findAllByUserFetchSong(User user);
    Optional<UserSong> findByUserAndSong(User user, Song song);
    Optional<UserSong> findByUserIdAndSongId(Long userId, Long songId);
}
