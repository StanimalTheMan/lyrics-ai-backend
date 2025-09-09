package com.jiggycode.service;

import com.jiggycode.entity.Song;
import com.jiggycode.entity.User;
import com.jiggycode.entity.UserSong;
import com.jiggycode.repository.SongRepository;
import com.jiggycode.repository.UserRepository;
import com.jiggycode.repository.UserSongRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserSongService {

    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final UserSongRepository userSongRepository;

    public UserSongService(UserRepository userRepository, SongRepository songRepository, UserSongRepository userSongRepository) {
        this.userRepository = userRepository;
        this.songRepository = songRepository;
        this.userSongRepository = userSongRepository;
    }

    public void saveSongForUser(String userEmail, Long songId) throws Exception {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new Exception("User not found"));

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new Exception("Song not found"));

        // Check if already saved
        boolean isSongExistsForUser = userSongRepository.existsByUserAndSong(user, song);
        if (isSongExistsForUser) {
            throw new Exception("Song already saved for user");
        }

        UserSong userSong = new UserSong();
        userSong.setUser(user);
        userSong.setSong(song);
        userSongRepository.save(userSong);
    }

    public List<Song> getSavedSongsForUser(String userEmail) throws Exception {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<UserSong> userSongs = userSongRepository.findAllByUserFetchSong(user);

        return userSongs.stream()
                .map(UserSong::getSong)
                .collect(Collectors.toList());
    }

    public void removeSongForUser(String userEmail, Long songId) throws Exception {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new Exception("User not found"));

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new Exception("Song not found"));

        UserSong userSong = userSongRepository.findByUserAndSong(user, song)
                .orElseThrow(() -> new Exception("UserSong not found"));

        userSongRepository.delete(userSong);
    }

    @Transactional(readOnly = true)
    public UserSong findByUserIdAndSongId(Long userId, Long songId) {
        UserSong userSong= userSongRepository.findByUserIdAndSongId(userId, songId)
                .orElseThrow(() -> new RuntimeException("UserSong not found"));
        
        return userSong;
    }

    @Transactional(readOnly = true)
    public Optional<Long> findUserIdByEmail(String email) {
        return userRepository.findByEmail(email).map(User::getId);
    }


    @Transactional
    public String getLyricsForUserSong(Long userId, Long songId) {
        UserSong userSong = userSongRepository.findByUserIdAndSongId(userId, songId)
                .orElseThrow(() -> new RuntimeException("UserSong not found"));
        if (userSong == null) {
            throw new RuntimeException("UserSong not found");
        }

        return userSong.getSong().getLyrics();
    }
}
