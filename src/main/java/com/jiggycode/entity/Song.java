package com.jiggycode.entity;

import jakarta.persistence.*;

import java.util.List;

@Table(name = "songs")
@Entity
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private long id;

    private String title;
    private String artist;

    @Lob
    @Column(name = "lyrics", columnDefinition = "TEXT")
    private String lyrics;

    private String spotifyUrl;
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public String getSpotifyUrl() {
        return spotifyUrl;
    }

    public void setSpotifyUrl(String spotifyUrl) {
        this.spotifyUrl = spotifyUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
