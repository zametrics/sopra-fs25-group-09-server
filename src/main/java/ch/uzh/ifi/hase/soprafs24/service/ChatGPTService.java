package ch.uzh.ifi.hase.soprafs24.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ChatGPTService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKey;
    private final String apiUrl = "https://api.openai.com/v1/chat/completions";

    // Map to hold per-(session, lang, type) pools of remaining words
    private final Map<String, Deque<String>> pools = new ConcurrentHashMap<>();

    public ChatGPTService() {
        this.apiKey = System.getenv("OPENAI_API_KEY");
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            throw new IllegalStateException("OpenAI API Key not set");
        }
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    private List<String> fetchWordPool(String lang, String type, int count) {
        String langLabel = switch (lang) {
            case "de" -> "German (no pronouns (der/die/das))";
            case "ch" -> "Swiss German (echtes Züridütsch) (no pronouns (der/die/das))";
            case "en" -> "English";
            default -> "English";
        };

        String prompt = String.format(
                "Give me %d random, drawable, (high-school to college difficulty) nouns in the category '%s' in %s. " +
                        "Only return a raw JSON array like [\"word1\", \"word2\", \"word3\"], with no additional wrapping object.",
                count, type, langLabel
        );

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-3.5-turbo");
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));
        body.put("temperature", 0.8);

        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                String rawResponse = webClient.post()
                        .bodyValue(body)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                JsonNode root = objectMapper.readTree(rawResponse);
                String content = root.path("choices").get(0)
                        .path("message").path("content")
                        .asText();

                // parse, remove any duplicates, shuffle, then return
                List<String> wordsRaw = objectMapper.readValue(content, new TypeReference<List<String>>() {});
                List<String> unique = new ArrayList<>(new LinkedHashSet<>(wordsRaw));
                Collections.shuffle(unique);
                return unique;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Fallback after all retries
        return List.of("apple", "dog", "house");
    }

    /**
     * Draws `count` words from the pool for (sessionId, lang, type).
     * Refills by calling fetchWordPool(..., 50) when needed.
     */
    public List<String> nextWords(String sessionId, String lang, String type, int count) {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new IllegalArgumentException("sessionId must be provided");
        }

        // Composite key so changing lang/type creates a fresh pool
        String poolKey = sessionId + "|" + lang + "|" + type;

        Deque<String> pool = pools.computeIfAbsent(poolKey,
                id -> new ArrayDeque<>(fetchWordPool(lang, type, 50))
        );

        if (pool.isEmpty()) {
            pool.addAll(fetchWordPool(lang, type, 50));
        }

        List<String> out = new ArrayList<>();
        for (int i = 0; i < count && !pool.isEmpty(); i++) {
            out.add(pool.pollFirst());
        }

        if (pool.size() < count) {
            pool.addAll(fetchWordPool(lang, type, 50));
        }

        return out;
    }
}