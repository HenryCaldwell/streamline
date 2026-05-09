package info.henrycaldwell.aggregator.transform;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import info.henrycaldwell.aggregator.core.MediaRef;
import info.henrycaldwell.aggregator.error.SpecException;

public class NoOpTransformerTest {

  @Nested
  class Constructor {

    @Test
    void acceptsMinimalConfig() {
      Config config = ConfigFactory.parseString("""
          name = transformer
          type = no_op
          """);

      assertDoesNotThrow(() -> new NoOpTransformer(config));
    }

    @Test
    void throwsOnUnknownKey() {
      Config config = ConfigFactory.parseString("""
          name = transformer
          type = no_op
          extra = value
          """);

      SpecException exception = assertThrows(SpecException.class, () -> new NoOpTransformer(config));

      assertTrue(exception.getMessage().contains("Unknown configuration key"));
      assertTrue(exception.getMessage().contains("key=extra"));
    }
  }

  @Nested
  class Transform {

    @Test
    void returnsMediaUnchanged() {
      MediaRef media = new MediaRef("clip-1", null, null, "Title", "Broadcaster", "en", null);
      Config config = ConfigFactory.parseString("""
          name = transformer
          type = no_op
          """);
      NoOpTransformer transformer = new NoOpTransformer(config);

      MediaRef result = transformer.transform(media);

      assertEquals(media, result);
    }
  }

  @Nested
  class Apply {

    @Test
    void returnsMediaUnchanged() {
      MediaRef media = new MediaRef("clip-1", null, null, "Title", "Broadcaster", "en", null);
      Config config = ConfigFactory.parseString("""
          name = transformer
          type = no_op
          """);
      NoOpTransformer transformer = new NoOpTransformer(config);

      MediaRef result = transformer.apply(media);

      assertEquals(media, result);
    }
  }
}
