package com.jiggycode.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "highlights")
public class Highlight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int startIndex;

    @Column(nullable = false)
    private int endIndex;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_song_id", nullable = false)
    private UserSong userSong;

    public Highlight() {}

    public Highlight(int startIndex, int endIndex, String explanation, UserSong userSong) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.explanation = explanation;
        this.userSong = userSong;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public UserSong getUserSong() {
        return userSong;
    }

    public void setUserSong(UserSong userSong) {
        this.userSong = userSong;
    }
}
