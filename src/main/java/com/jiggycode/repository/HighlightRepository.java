package com.jiggycode.repository;

import com.jiggycode.entity.Highlight;
import com.jiggycode.entity.UserSong;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HighlightRepository extends JpaRepository<Highlight, Long> {
    List<Highlight> findByUserSong(UserSong userSong);
}
