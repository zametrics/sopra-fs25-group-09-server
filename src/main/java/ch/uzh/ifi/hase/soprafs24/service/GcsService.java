package ch.uzh.ifi.hase.soprafs24.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@Service
public class GcsService {

    private final String BUCKET_NAME = "sopra-fs25-group-09-server.appspot.com"; // bucket name google cloud storage

    private final Storage storage;

    public GcsService() throws IOException {
        String base64Key = System.getenv("GCS_KEY_BASE64");
        if (base64Key == null || base64Key.isEmpty()) {
            throw new IllegalStateException("Environment variable GCS_KEY_BASE64 not set.");
        }

        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        ByteArrayInputStream credentialsStream = new ByteArrayInputStream(decodedKey);

        this.storage = StorageOptions.newBuilder()
                .setCredentials(ServiceAccountCredentials.fromStream(credentialsStream))
                .build()
                .getService();
    }


    public String uploadAvatar(Long userId, MultipartFile file) throws IOException {
        // Timestamp zur Vermeidung von Überschreibungen & Cache-Problemen
        String timestamp = String.valueOf(System.currentTimeMillis());
        String objectName = String.format("avatar/%d_%s", userId, timestamp);

        BlobId blobId = BlobId.of(BUCKET_NAME, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .setCacheControl("no-cache") // Optional: verhindert Browser-Caching
                .build();

        storage.create(blobInfo, file.getInputStream());

        // Gib die öffentlich zugängliche URL zurück
        return String.format("https://storage.googleapis.com/%s/%s", BUCKET_NAME, objectName);
    }

}
