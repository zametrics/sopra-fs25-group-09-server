package ch.uzh.ifi.hase.soprafs24.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;



import com.google.cloud.storage.BlobInfo;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class GcsServiceTest {

    private GcsService gcsService;
    private Storage mockStorage;

    @BeforeEach
    void setUp() throws Exception {
        // Mock the Google Cloud Storage client
        mockStorage = mock(Storage.class);

        // Instantiate the real GcsService (will attempt to read env var, make sure it is set in your test environment)
        gcsService = new GcsService();

        // Use reflection to overwrite the private final 'storage' field with the mock
        Field storageField = GcsService.class.getDeclaredField("storage");
        storageField.setAccessible(true);
        storageField.set(gcsService, mockStorage);
    }

    @Test
    void uploadAvatar_returnsCorrectUrl() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getContentType()).thenReturn("image/png");
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

            when(mockStorage.create(
                any(BlobInfo.class),
                any(InputStream.class),
                (Storage.BlobWriteOption[]) any()
            )).thenReturn(null);
        String resultUrl = gcsService.uploadAvatar(42L, mockFile);

        assertTrue(resultUrl.startsWith("https://storage.googleapis.com/sopra-fs25-group-09-server.appspot.com/avatar/42_"));
    }
}
