package com.jiggycode.service;

import com.jiggycode.entity.Highlight;
import com.jiggycode.entity.UserSong;
import com.jiggycode.repository.HighlightRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HighlightService {

    private final HighlightRepository highlightRepository;


    public HighlightService(HighlightRepository highlightRepository) {
        this.highlightRepository = highlightRepository;
    }

    @Transactional(readOnly = true)
    public List<Highlight> findByUserSong(UserSong userSong) {
        if (userSong == null) {
            
            return List.of();
        }
        List<Highlight> highlights = highlightRepository.findByUserSong(userSong);

        return highlights;
    }

    public Highlight saveHighlight(Highlight highlight) {
        return highlightRepository.save(highlight);
    }
}
