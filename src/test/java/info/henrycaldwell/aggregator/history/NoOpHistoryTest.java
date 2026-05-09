package info.henrycaldwell.aggregator.history;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import info.henrycaldwell.aggregator.error.SpecException;

public class NoOpHistoryTest {

  @Nested
  class Constructor {

    @Test
    void acceptsMinimalConfig() {
      Config config = ConfigFactory.parseString("""
          name = history
          type = no_op
          """);

      assertDoesNotThrow(() -> new NoOpHistory(config));
    }

    @Test
    void throwsOnUnknownKey() {
      Config config = ConfigFactory.parseString("""
          name = history
          type = no_op
          extra = value
          """);

      SpecException exception = assertThrows(SpecException.class, () -> new NoOpHistory(config));

      assertTrue(exception.getMessage().contains("Unknown configuration key"));
      assertTrue(exception.getMessage().contains("key=extra"));
    }
  }

  @Nested
  class Claim {

    @Test
    void returnsTrue() {
      Config config = ConfigFactory.parseString("""
          name = history
          type = no_op
          """);
      NoOpHistory history = new NoOpHistory(config);

      assertTrue(history.claim("clip-1", "runner"));
    }
  }

  @Nested
  class Prepare {

    @Test
    void doesNothing() {
      Config config = ConfigFactory.parseString("""
          name = history
          type = no_op
          """);
      NoOpHistory history = new NoOpHistory(config);

      assertDoesNotThrow(() -> history.prepare("clip-1", "runner"));
    }
  }

  @Nested
  class Publish {

    @Test
    void doesNothing() {
      Config config = ConfigFactory.parseString("""
          name = history
          type = no_op
          """);
      NoOpHistory history = new NoOpHistory(config);

      assertDoesNotThrow(() -> history.publish("clip-1", "runner"));
    }
  }

  @Nested
  class Fail {

    @Test
    void doesNothing() {
      Config config = ConfigFactory.parseString("""
          name = history
          type = no_op
          """);
      NoOpHistory history = new NoOpHistory(config);

      assertDoesNotThrow(() -> history.fail("clip-1", "runner", "error message"));
    }
  }
}
