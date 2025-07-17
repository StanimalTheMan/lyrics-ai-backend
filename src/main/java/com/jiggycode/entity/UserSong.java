package com.jiggycode.entity;

import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@Entity
public class UserSong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Song song;

    @OneToMany(mappedBy = "userSong", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Highlight> highlights;

    private Date savedAt;
}
