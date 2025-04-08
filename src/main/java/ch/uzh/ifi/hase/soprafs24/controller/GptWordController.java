package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.service.ChatGPTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/words/gpt")
public class GptWordController {

    @Autowired
    private ChatGPTService chatGPTService;

    @GetMapping
    public ResponseEntity<List<String>> getWords(
            @RequestParam String lang,
            @RequestParam String type,
            @RequestParam(defaultValue = "3") int count
    ) {
        List <String> words = chatGPTService.generateWords(lang,type,count);
        return ResponseEntity.ok(words);
    }
}
