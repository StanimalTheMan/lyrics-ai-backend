package com.jiggycode;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LyricsServiceTest {

  @Test
  public void testNormalizeInput_RemoveExtraSpaces() {
      assertEquals("What Is Love?", LyricsService.normalizeInput(" What   Is Love? "));
      assertEquals("hello world", LyricsService.normalizeInput(" hello   world "));
      assertEquals("hello", LyricsService.normalizeInput("    hello   "));
  }

  @Test
  public void testNormalizeInput_NullInput() {
      assertNull(LyricsService.normalizeInput(null));
  }
}