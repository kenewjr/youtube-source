package dev.lavalink.youtube.clients;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IosTest {
    @Test
    void playerPayloadUsesAcceptedClientWithoutDeprecatedParams() {
        Ios client = new Ios();
        AndroidMusic musicClient = new AndroidMusic();
        String payload = Ios.BASE_CONFIG.copy()
            .withRootField("videoId", "au2A3wyiLIo")
            .toJsonString();

        assertEquals("21.32.4", Ios.CLIENT_VERSION);
        assertEquals("21.02.35", Android.CLIENT_VERSION);
        assertNotNull(client.getPlayerParams());
        assertNull(musicClient.getPlayerParams());
        assertTrue(payload.contains("\"clientName\":\"IOS\""));
        assertTrue(payload.contains("\"clientVersion\":\"21.32.4\""));
        assertFalse(payload.contains("\"params\""));
    }
}
