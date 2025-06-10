package com.jiggycode.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SongDetails {
    private TrackInfo trackInfo;
    private String lyrics;
}
