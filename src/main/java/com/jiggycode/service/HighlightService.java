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
            System.out.println("findByUserSong called with null userSong!");
            return List.of();
        }
        List<Highlight> highlights = highlightRepository.findByUserSong(userSong);
        System.out.println("Highlights found: " + highlights.size());
        highlights.forEach(h -> System.out.println("Highlight explanation: " + h.getExplanation()));
        return highlights;
    }

    public Highlight saveHighlight(Highlight highlight) {
        return highlightRepository.save(highlight);
    }
}
