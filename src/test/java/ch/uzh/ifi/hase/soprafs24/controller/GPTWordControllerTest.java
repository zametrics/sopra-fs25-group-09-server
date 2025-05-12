package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.service.ChatGPTService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GptWordController.class)
public class GPTWordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatGPTService chatGPTService;

    // Test: Successful word fetch
    @Test
    public void getWords_validInput_returnsWordList() throws Exception {
        // given
        List<String> sampleWords = List.of("apple", "banana", "grape");
        given(chatGPTService.nextWords(anyString(), eq("en"), eq("nouns"), eq(3)))
                .willReturn(sampleWords);

        // when
        MockHttpServletRequestBuilder getRequest = get("/api/words/gpt")
                .param("lang", "en")
                .param("type", "nouns")
                .param("count", "3")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", containsInAnyOrder("apple", "banana", "grape")));
    }

    // Test: API error handling
    @Test
    public void getWords_chatGPTFails_returnsFallbackWords() throws Exception {
        // given
        given(chatGPTService.nextWords(anyString(), eq("de"), eq("nouns"), eq(3)))
                .willReturn(List.of("Error", "when", "requesting", "words"));

        // when
        MockHttpServletRequestBuilder getRequest = get("/api/words/gpt")
                .param("lang", "de")
                .param("type", "nouns")
                .param("count", "3")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", containsInAnyOrder("Error", "when", "requesting", "words")));
    }

    // Test: Missing required parameters
    @Test
    public void getWords_missingParams_returnsBadRequest() throws Exception {
        // when
        MockHttpServletRequestBuilder getRequest = get("/api/words/gpt")
                .param("lang", "en")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isBadRequest());
    }

    // Test: Missing API key
    @Test
    public void getWords_missingAPIKey_returnsErrorWords() throws Exception {
        // given
        given(chatGPTService.nextWords(anyString(), eq("en"), eq("nouns"), eq(3)))
                .willReturn(List.of("Error", "when", "requesting", "words"));

        // when
        MockHttpServletRequestBuilder getRequest = get("/api/words/gpt")
                .param("lang", "en")
                .param("type", "nouns")
                .param("count", "3")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", containsInAnyOrder("Error", "when", "requesting", "words")));
    }
}
