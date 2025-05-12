package ch.uzh.ifi.hase.soprafs24.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
            case "de" -> "German";
            case "ch" -> "Swiss German (echtes Züridütsch)";
            case "en" -> "English";
            default -> "English";
        };

        String prompt = String.format(
                "Give me %d random, drawable, elementary-level nouns in the category '%s' in %s. " +
                        "Only return a raw JSON array like [\"word1\",\"word2\",...], nothing else.",
                count, type, langLabel
        );

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-3.5-turbo");
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));
        body.put("temperature", 0.8);

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
            return objectMapper.readValue(content, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            return List.of("Error fetching pool:", sw.toString());
        }
    }

    /**
     * Draws `count` words from the pool for (sessionId, lang, type).
     * Refills by calling fetchWordPool(..., 50) when needed.
     */
    public List<String> nextWords(String sessionId, String lang, String type, int count) {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new IllegalArgumentException("sessionId must be provided");
        }

        // composite key so changing lang/type creates a fresh pool
        String poolKey = sessionId + "|" + lang + "|" + type;

        Deque<String> pool = pools.computeIfAbsent(poolKey,
                id -> new ArrayDeque<>(fetchWordPool(lang, type, 50))
        );

        List<String> out = new ArrayList<>();
        for (int i = 0; i < count && !pool.isEmpty(); i++) {
            out.add(pool.pollFirst());
        }

        if (pool.size() < count) {
            pools.put(poolKey, new ArrayDeque<>(fetchWordPool(lang, type, 50)));
        }

        return out;
    }
}
