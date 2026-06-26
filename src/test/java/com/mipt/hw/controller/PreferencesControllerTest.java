package com.mipt.hw.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PreferencesController.class)
class PreferencesControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void shouldGetViewPreference() throws Exception {
    // When & Then
    mockMvc.perform(get("/api/preferences/view")
        .cookie(new jakarta.servlet.http.Cookie("viewPreference", "detailed")))
      .andExpect(status().isOk())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(jsonPath("$.viewMode").value("detailed"));
  }

  @Test
  void shouldReturnDefaultViewPreferenceWhenNoCookie() throws Exception {
    // When & Then
    mockMvc.perform(get("/api/preferences/view"))
      .andExpect(status().isOk())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(jsonPath("$.viewMode").value("detailed"));
  }

  @Test
  void shouldSetViewPreference() throws Exception {
    // When & Then
    mockMvc.perform(post("/api/preferences/view")
        .param("mode", "compact"))
      .andExpect(status().isOk())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(jsonPath("$.viewMode").value("compact"))
      .andExpect(jsonPath("$.message").value("View preference updated successfully"));
  }

  @Test
  void shouldReturn400WhenInvalidMode() throws Exception {
    // When & Then
    mockMvc.perform(post("/api/preferences/view")
        .param("mode", "invalid"))
      .andExpect(status().isBadRequest())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(jsonPath("$.error").value("Invalid mode. Use 'compact' or 'detailed'"));
  }

  @Test
  void shouldResetViewPreference() throws Exception {
    // When & Then
    mockMvc.perform(delete("/api/preferences/view"))
      .andExpect(status().isOk())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(jsonPath("$.viewMode").value("detailed"))
      .andExpect(jsonPath("$.message").value("View preference reset to default"));
  }

  @Test
  void shouldGetAllPreferences() throws Exception {
    // When & Then
    mockMvc.perform(get("/api/preferences/all"))
      .andExpect(status().isOk())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(jsonPath("$.viewMode").exists())
      .andExpect(jsonPath("$.favoritesCount").exists())
      .andExpect(jsonPath("$.sessionId").exists());
  }
}
