import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.YoutubeSourceOptions;
import dev.lavalink.youtube.clients.MWeb;
import dev.lavalink.youtube.clients.skeleton.Client;
import dev.lavalink.youtube.track.format.StreamFormat;
import dev.lavalink.youtube.track.format.TrackFormats;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.net.URI;

/**
 * test for remote cipher and content bound remote PoToken playback.
 */
public class RemoteMWebPlaybackTest {
    private static final String Id = "iiEt0wrFehA";
    private static final String RemotePotBase = System.getenv().getOrDefault("WEBPO_URL", "http://127.0.0.1:8080");
    private static final String RemotePotPassword = System.getenv("WEBPO_TOKEN");
    private static final String CipherUrl = "https://cipher.kikkia.dev/";
    private static final String AGENT = "cipher/1.0";

    @Test
    @EnabledIfEnvironmentVariable(named = "YOUTUBE_REMOTE_PLAYBACK_TEST", matches = "true")
    public void testMWebPlaybackUrl() throws Throwable {

        Client client = new MWeb();

        YoutubeSourceOptions options = new YoutubeSourceOptions()
            .setRemotePoToken(RemotePotBase, RemotePotPassword)
            .setRemoteCipher(CipherUrl, null, AGENT);
        YoutubeAudioSourceManager source = new YoutubeAudioSourceManager(options, client);

        try (HttpInterface httpInterface = source.getInterface()) {
            TrackFormats formats = client.loadFormats(source, httpInterface, Id);

            Assertions.assertNotNull(formats);
            Assertions.assertFalse(formats.getFormats().isEmpty(), "MWEB returned no formats");
            Assertions.assertNotNull(formats.getPoToken(), "Remote PoToken was not generated");

            StreamFormat format = formats.getBestFormat();
            Assertions.assertNotNull(format.getUrl(), "MWEB returned a format without a playback URL");

            URI resolvedUrl = source.getCipherManager().resolveFormatUrl(
                httpInterface,
                formats.getPlayerScriptUrl(),
                format);

            URI playableUrl = client.transformPlaybackUri(format.getUrl(), resolvedUrl, formats.getPoToken());

            Assertions.assertNotNull(playableUrl.getHost());

            Assertions.assertTrue(playableUrl.getHost().contains("googlevideo.com"),
                "Resolved url is not a google video url: " + playableUrl);
            Assertions.assertTrue(playableUrl.getQuery().contains("pot="),
                "Resolved MWEB url does not contain the content-bound PoToken");

            HttpGet playbackRequest = new HttpGet(playableUrl);

            try (CloseableHttpResponse response = httpInterface.execute(playbackRequest)) {
                int statusCode = response.getStatusLine().getStatusCode();
                Assertions.assertTrue(statusCode == 200 || statusCode == 206, "Resolved MWEB URL was not playable; status code: " + statusCode);
            }
        }
    }

}
