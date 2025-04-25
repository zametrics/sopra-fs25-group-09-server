package ch.uzh.ifi.hase.soprafs24.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

@Service
public class ChatGPTService {

    private final WebClient webClient;   //standard spring http client
    private final ObjectMapper objectMapper = new ObjectMapper();   //JSON parser
    private final String apiKey;    //this will be read from Github Secrets (line 21)
    private final String apiUrl = "https://api.openai.com/v1/chat/completions";   //prompts get sent to this endpoint

    public ChatGPTService() {
        this.apiKey = System.getenv("OPENAI_API_KEY");   //load api Key from system env;
        if(this.apiKey == null || this.apiKey.isEmpty()) {
            throw new IllegalStateException("OpenAI API Key not set");
        }

        this.webClient = WebClient.builder()   //configure webclient
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();
    }

    public List <String> generateWords(String lang, String type, int count) {  //we can easily add here more languages
        String langLabel = switch (lang) {
            case "de" -> "German";
            case "ch" -> "Swiss German (Züridütsch)";
            case "en" -> "english";
            default -> "english";
        };
        
        String prompt = String.format(
            "Give me %d very simple %s nouns in %s. Make sure they vary and avoid using common ones. " +
            "The chances that you already said them should not be high. Return the words as a JSON array, " +
            "e.g., [\"word1\", \"word2\", \"word3\"], with no additional wrapping object.",
            count, type, langLabel
        );

        Map<String, Object> requestBody = new HashMap<>();  //this map holds the body of the request
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)   //sends prompt as message from the user
        ));
        requestBody.put("temperature", 0.8);  // temperature controls randomness, where low is predictable and high unpredictable

        try{
            String response = webClient.post()  //send post request with prompt model and the other stuff
                    .bodyValue(requestBody)
                    .retrieve()   //get the http response
                    .bodyToMono(String.class)// "i want body as a string!"
                    .block();  //waits until response done(sync call)

            JsonNode json = objectMapper.readTree(response);  //parses raw json string into json tree
            String content = json
                    .path("choices").get(0)     //first item
                    .path("message")
                    .path("content")
                    .asText();                 //Goes inside the json and finds the actual words you asked ChatJibity for

            //parse json array string from GPT response
            return objectMapper.readValue(content, new TypeReference<>() {});  //content is herre still just a JSON string so we turn it into a "real" List<String> (Java List)
        } catch (Exception e) {
            e.printStackTrace(); // Print to console (System.err)
             // Capture stack trace as a String
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stackTrace = sw.toString();

            // Activate Return statement for debugging 
            return List.of("Error", "exception","API KEY",this.apiKey, stackTrace);
    }

}}
