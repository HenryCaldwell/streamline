package info.henrycaldwell.streamline.retrieve;

import java.util.List;

import com.typesafe.config.Config;

import info.henrycaldwell.streamline.config.Spec;
import info.henrycaldwell.streamline.core.ClipRef;

/**
 * Class for retrieving clips by performing no action.
 *
 * This class accepts configuration without retrieving clips from an external
 * source.
 */
public final class NoOpRetriever extends AbstractRetriever {

  public static final Spec SPEC = Spec.builder().build();

  /**
   * Constructs a NoOpRetriever.
   *
   * @param config A {@link Config} representing the retriever configuration.
   */
  public NoOpRetriever(Config config) {
    super(config, SPEC);
  }

  /**
   * Retrieves clips by performing no action.
   *
   * @return A {@link List} of {@link ClipRef} representing the retrieved clips.
   */
  @Override
  public List<ClipRef> fetch() {
    return List.of();
  }
}
