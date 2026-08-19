package dev.lavalink.youtube.clients;

import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.Units;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebPlaylistTest {
    @Test
    void extractsCurrentLockupPlaylistItems() throws Exception {
        TestWeb client = new TestWeb();
        YoutubeAudioSourceManager source = new YoutubeAudioSourceManager(client);

        try {
            JsonBrowser videoList = client.extractVideoList(JsonBrowser.parse("""
                {
                  "contents": {
                    "twoColumnBrowseResultsRenderer": {
                      "tabs": [{
                        "tabRenderer": {
                          "content": {
                            "sectionListRenderer": {
                              "contents": [{
                                "itemSectionRenderer": {
                                  "contents": [{
                                    "lockupViewModel": {
                                      "contentId": "x1UsJ2Znjk0",
                                      "contentType": "LOCKUP_CONTENT_TYPE_VIDEO",
                                      "contentImage": {
                                        "thumbnailViewModel": {
                                          "overlays": [{
                                            "thumbnailBottomOverlayViewModel": {
                                              "badges": [{
                                                "thumbnailBadgeViewModel": {
                                                  "rendererContext": {
                                                    "accessibilityContext": {
                                                      "label": "4 minutes, 50 seconds"
                                                    }
                                                  }
                                                }
                                              }]
                                            }
                                          }]
                                        }
                                      },
                                      "metadata": {
                                        "lockupMetadataViewModel": {
                                          "title": {"content": "Modern title"},
                                          "metadata": {
                                            "contentMetadataViewModel": {
                                              "metadataRows": [{
                                                "metadataParts": [{
                                                  "text": {"content": "Modern artist"}
                                                }]
                                              }]
                                            }
                                          }
                                        }
                                      }
                                    }
                                  }]
                                }
                              }]
                            }
                          }
                        }
                      }]
                    }
                  }
                }
                """));

            List<AudioTrack> tracks = client.extractTracks(videoList, source);

            assertEquals(1, tracks.size());
            assertEquals("x1UsJ2Znjk0", tracks.get(0).getIdentifier());
            assertEquals("Modern title", tracks.get(0).getInfo().title);
            assertEquals("Modern artist", tracks.get(0).getInfo().author);
            assertEquals(Units.DURATION_MS_UNKNOWN, tracks.get(0).getDuration());
        } finally {
            source.shutdown();
        }
    }

    @Test
    void preservesLegacyPlaylistVideoRendererItems() throws Exception {
        TestWeb client = new TestWeb();
        YoutubeAudioSourceManager source = new YoutubeAudioSourceManager(client);

        try {
            JsonBrowser videoList = client.extractVideoList(JsonBrowser.parse("""
                {
                  "contents": {
                    "twoColumnBrowseResultsRenderer": {
                      "tabs": [{
                        "tabRenderer": {
                          "content": {
                            "sectionListRenderer": {
                              "contents": [{
                                "itemSectionRenderer": {
                                  "contents": [{
                                    "playlistVideoListRenderer": {
                                      "contents": [{
                                        "playlistVideoRenderer": {
                                          "videoId": "SEyAq7-Ertw",
                                          "isPlayable": true,
                                          "lengthSeconds": "280",
                                          "title": {"simpleText": "Legacy title"},
                                          "shortBylineText": {
                                            "runs": [{"text": "Legacy artist"}]
                                          }
                                        }
                                      }]
                                    }
                                  }]
                                }
                              }]
                            }
                          }
                        }
                      }]
                    }
                  }
                }
                """));

            List<AudioTrack> tracks = client.extractTracks(videoList, source);

            assertEquals(1, tracks.size());
            assertEquals("SEyAq7-Ertw", tracks.get(0).getIdentifier());
            assertEquals("Legacy title", tracks.get(0).getInfo().title);
            assertEquals("Legacy artist", tracks.get(0).getInfo().author);
            assertEquals(280_000, tracks.get(0).getDuration());
        } finally {
            source.shutdown();
        }
    }

    @Test
    void extractsCurrentViewModelContinuationToken() throws Exception {
        TestWeb client = new TestWeb();
        JsonBrowser videoList = JsonBrowser.parse("""
            [{
              "lockupViewModel": {"contentId": "video"}
            }, {
              "continuationItemViewModel": {
                "continuationCommand": {
                  "innertubeCommand": {
                    "continuationCommand": {"token": "modern-token"}
                  }
                }
              }
            }]
            """);

        assertEquals("modern-token", client.extractContinuationToken(videoList));
    }

    @Test
    void preservesLegacyContinuationToken() throws Exception {
        TestWeb client = new TestWeb();
        JsonBrowser videoList = JsonBrowser.parse("""
            {
              "contents": [{
                "continuationItemRenderer": {
                  "continuationEndpoint": {
                    "continuationCommand": {"token": "legacy-token"}
                  }
                }
              }]
            }
            """);

        assertEquals("legacy-token", client.extractContinuationToken(videoList));
    }

    private static final class TestWeb extends Web {
        private JsonBrowser extractVideoList(JsonBrowser response) {
            return extractPlaylistVideoList(response);
        }

        private List<AudioTrack> extractTracks(JsonBrowser videoList, YoutubeAudioSourceManager source) {
            List<AudioTrack> tracks = new ArrayList<>();
            extractPlaylistTracks(videoList, tracks, source);
            return tracks;
        }

        private String extractContinuationToken(JsonBrowser videoList) {
            return extractPlaylistContinuationToken(videoList);
        }
    }
}