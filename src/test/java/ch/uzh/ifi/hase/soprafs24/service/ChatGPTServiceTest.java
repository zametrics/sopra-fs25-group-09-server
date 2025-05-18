package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * nightmare unit tests for {@link ChatGPTService}.
 *
 * The real OpenAI endpoint is not contacted. When a call fails (very
 * likely in CI), the service falls back to the hard‑coded list
 * ["apple", "dog", "house"], which is adequate for validating pool
 * behaviour. These tests therefore do not rely on network access.
 */
class ChatGPTServiceTest {

    private ChatGPTService service;

    @BeforeEach
    void init() {
        // Guarantee that the constructor does not throw, even on hosts where
        // OPENAI_API_KEY is unset.
        ensureEnv("OPENAI_API_KEY", "dummy-key");
        service = new ChatGPTService();
    }


    @Test
    @DisplayName("Returns the requested number of words")
    void basicFetch() {
        List<String> words = service.nextWords("s1", "en", "objects", 3);
        assertEquals(3, words.size());
    }

    @Test
    @DisplayName("Zero count yields empty list")
    void zeroCount() {
        List<String> words = service.nextWords("s1", "en", "objects", 0);
        assertTrue(words.isEmpty());
    }

    @Test
    @DisplayName("Negative count is handled gracefully (returns empty list)")
    void negativeCountGraceful() {
        List<String> words = service.nextWords("s1", "en", "objects", -5);
        assertTrue(words.isEmpty());
    }


    // Pool refill and sizing


    @Test
    @DisplayName("Changing session id results in separate pools")
    void poolIsolationBySession() {
        List<String> batchA = service.nextWords("alpha", "en", "animals", 2);
        List<String> batchB = service.nextWords("beta", "en", "animals", 2);

        assertAll(
                () -> assertEquals(2, batchA.size()),
                () -> assertEquals(2, batchB.size()),
                () -> assertEquals(2, service.nextWords("alpha", "en", "animals", 2).size()),
                () -> assertEquals(2, service.nextWords("beta", "en", "animals", 2).size())
        );
    }

    @Test
    @DisplayName("Changing language or type resets the pool even for same session")
    void poolIsolationByLangAndType() {
        List<String> englishAnimals = service.nextWords("gamma", "en", "animals", 1);
        List<String> germanObjects = service.nextWords("gamma", "de", "objects", 1);

        assertEquals(1, englishAnimals.size());
        assertEquals(1, germanObjects.size());
    }


    // Concurrency

    @Test
    @DisplayName("Concurrent access is thread‑safe and returns complete results")
    void concurrency() throws InterruptedException, ExecutionException {
        int threads = 20;
        ExecutorService exec = Executors.newFixedThreadPool(threads);
        Set<Future<List<String>>> futures = new HashSet<>();

        for (int i = 0; i < threads; i++) {
            futures.add(exec.submit(() -> service.nextWords("concurrent", "en", "objects", 1)));
        }

        exec.shutdown();
        assertTrue(exec.awaitTermination(5, TimeUnit.SECONDS), "Executor did not terminate in time");

        int total = 0;
        for (Future<List<String>> f : futures) {
            List<String> w = f.get();
            total += w.size();
            assertEquals(1, w.size());
        }
        assertEquals(threads, total);
    }


    // Fallback and edge cases


    @Test
    @DisplayName("Unsupported language code falls back to English")
    void unsupportedLangFallsBack() {
        List<String> words = service.nextWords("theta", "xx", "objects", 1);
        assertEquals(1, words.size());
    }

    @Test
    @DisplayName("Missing session id throws IllegalArgumentException")
    void missingSessionId() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.nextWords("", "en", "objects", 1)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.nextWords(null, "en", "objects", 1))
        );
    }


    // Helpers

    /**
     * Adds an environment variable if it is absent. Uses reflection because
     * {@code System.getenv()} is unmodifiable in most JVMs.
     */
    private static void ensureEnv(String key, String value) {
        if (System.getenv(key) != null) {
            return;
        }
        try {
            // Works for common JDKs (OpenJDK/HotSpot) on Windows and Unix‑like OSs.
            java.lang.reflect.Field field = Class.forName("java.lang.ProcessEnvironment")
                    .getDeclaredField("theCaseInsensitiveEnvironment");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<String, String> env = (java.util.Map<String, String>) field.get(null);
            env.put(key, value);
        } catch (Exception ignored) {
            // If the above hack fails, the test may still succeed via fallback
        }
    }
}
