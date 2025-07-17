package com.jiggycode.repository;

import com.jiggycode.entity.UserSong;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSongRepository extends JpaRepository<UserSong, Long> {
    List<UserSong> findByUserId(Long userId);
    Optional<UserSong> findByUserIdAndSongId(Long userId, Long songId);
}
