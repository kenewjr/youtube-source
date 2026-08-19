package dev.lavalink.youtube.track;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

class YoutubePersistentHttpStreamTest {
    @Test
    void rangeEndIsInclusiveAndNeverExceedsContentLength() throws Exception {
        try (TestStream stream = new TestStream(URI.create("https://example.com/audio?foo=bar"), 100)) {
            assertEquals("foo=bar&range=0-99", stream.connectUrl().getQuery());
        }
    }

    private static final class TestStream extends YoutubePersistentHttpStream {
        private TestStream(URI contentUrl, long contentLength) {
            super(null, contentUrl, contentLength);
        }

        private URI connectUrl() {
            return getConnectUrl();
        }
    }
}
