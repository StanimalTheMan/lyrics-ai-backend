package com.jiggycode.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackInfo {
    private String track;
    private String artist;
    private String spotifyUrl;
    private String imageUrl;
}
