package info.henrycaldwell.aggregator.download;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import info.henrycaldwell.aggregator.core.ClipRef;
import info.henrycaldwell.aggregator.core.MediaRef;
import info.henrycaldwell.aggregator.error.SpecException;

public class NoOpDownloaderTest {

  @TempDir
  Path tempDir;

  @Nested
  class Constructor {

    @Test
    void acceptsMinimalConfig() {
      Config config = ConfigFactory.parseString("""
          name = downloader
          type = no_op
          """);

      assertDoesNotThrow(() -> new NoOpDownloader(config));
    }

    @Test
    void throwsOnUnknownKey() {
      Config config = ConfigFactory.parseString("""
          name = downloader
          type = no_op
          extra = value
          """);

      SpecException exception = assertThrows(SpecException.class, () -> new NoOpDownloader(config));

      assertTrue(exception.getMessage().contains("Unknown configuration key"));
      assertTrue(exception.getMessage().contains("key=extra"));
    }
  }

  @Nested
  class Download {

    @Test
    void returnsMediaRefWithTargetPath() {
      Path target = tempDir.resolve("clip.mp4");
      ClipRef clip = new ClipRef("clip-1", "https://example.com/clip-1", "Title", "Broadcaster", "en", 0, List.of("tag"));
      Config config = ConfigFactory.parseString("""
          name = downloader
          type = no_op
          """);
      NoOpDownloader downloader = new NoOpDownloader(config);

      MediaRef result = downloader.download(clip, target);

      assertEquals("clip-1", result.id());
      assertEquals(target, result.file());
      assertEquals("Title", result.title());
      assertEquals("Broadcaster", result.broadcaster());
      assertEquals("en", result.language());
      assertEquals(List.of("tag"), result.tags());
    }
  }
}
