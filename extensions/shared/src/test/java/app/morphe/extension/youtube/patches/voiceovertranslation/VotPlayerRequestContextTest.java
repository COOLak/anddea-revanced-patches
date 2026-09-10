package app.morphe.extension.youtube.patches.voiceovertranslation;

import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class VotPlayerRequestContextTest {
    @Test public void retainsOnlyPlayerCredentialsForExactVideo() {
        String id = "AbCdEfGhI_1";
        assertEquals(id, VotPlayerRequestContext.put("https://youtubei.googleapis.com/youtubei/v1/player?id=" + id,
                Map.of("authorization", "test-credential", "Cookie", "never-retain", "X-Goog-PageId", "test-page")));
        assertEquals(Map.of("Authorization", "test-credential", "X-Goog-PageId", "test-page"), VotPlayerRequestContext.get(id));
        assertTrue(VotPlayerRequestContext.get("other-video").isEmpty());
    }
    @Test public void rejectsForeignHostsAndNonPlayerRequests() {
        for (String url : new String[]{
                "https://youtube.com.example/youtubei/v1/player?id=AbCdEfGhI_2",
                "https://example.com/youtubei/v1/player?id=AbCdEfGhI_2",
                "http://youtube.com/youtubei/v1/player?id=AbCdEfGhI_2",
                "https://youtube.com/youtubei/v1/browse?id=AbCdEfGhI_2"}) {
            assertNull(VotPlayerRequestContext.put(url, Map.of("Authorization", "test")));
        }
        assertTrue(VotPlayerRequestContext.get("AbCdEfGhI_2").isEmpty());
    }
}
