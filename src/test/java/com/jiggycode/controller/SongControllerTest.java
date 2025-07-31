package com.jiggycode.controller;

import com.jiggycode.config.JwtAuthenticationFilter;
import com.jiggycode.entity.Song;
import com.jiggycode.service.SongService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SongController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
})
@AutoConfigureMockMvc(addFilters = false)
public class SongControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SongService songService;

    @Test
    void testGetOrSaveSong_success() throws Exception {
        Song mockSong = new Song();
        mockSong.setId(1L);
        mockSong.setTitle("Imagine");
        mockSong.setArtist("John Lennon");

        Mockito.when(songService.getOrSaveSong("Imagine", "John Lennon"))
                .thenReturn(mockSong);

        mockMvc.perform(get("/api/songs/details")
                        .param("track", "Imagine")
                        .param("artist", "John Lennon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Imagine"))
                .andExpect(jsonPath("$.artist").value("John Lennon"));
    }
}
