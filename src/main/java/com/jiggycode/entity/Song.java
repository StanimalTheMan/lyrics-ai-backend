package com.jiggycode.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Table(name = "songs")
@Entity
public class Song {

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private long id;

    @Setter
    @Getter
    private String title;
    @Setter
    @Getter
    private String artist;

    @Setter
    @Getter
    @Lob
    @Column(name = "lyrics", columnDefinition = "TEXT")
    private String lyrics;

    @Setter
    @Getter
    private String spotifyUrl;
    @Setter
    @Getter
    private String imageUrl;

    @OneToMany(mappedBy = "song", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserSong> userSongs;

    // Default constructor (required by JPA)
    public Song() {}

    public Song(String title, String artist, String lyrics, String spotifyUrl, String imageUrl) {
        this.title = title;
        this.artist = artist;
        this.lyrics = lyrics;
        this.spotifyUrl = spotifyUrl;
        this.imageUrl = imageUrl;
    }

}
