package ch.uzh.ifi.hase.soprafs24.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ch.uzh.ifi.hase.soprafs24.service.GcsService;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*") // Für React-Frontend, bei Bedarf einschränken
public class FileController {

    private final GcsService gcsService;

    public FileController(GcsService gcsService) {
        this.gcsService = gcsService;
    }
    

    @PostMapping("/upload-avatar/{userId}")
public ResponseEntity<String> uploadAvatar(
        @PathVariable Long userId,
        @RequestParam("file") MultipartFile file) {
    try {
        String url = gcsService.uploadAvatar(userId, file);
        return ResponseEntity.ok(url);
    } catch (Exception e) {
        return ResponseEntity.status(500).body("Fehler beim Upload: " + e.getMessage());
    }
}

}
