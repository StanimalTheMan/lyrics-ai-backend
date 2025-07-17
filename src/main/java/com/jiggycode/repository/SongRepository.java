package com.jiggycode.repository;

import com.jiggycode.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SongRepository extends JpaRepository<Song, Long> {
    Optional<Song> findByTitleIgnoreCaseAndArtistIgnoreCase(String title, String artist);
}
