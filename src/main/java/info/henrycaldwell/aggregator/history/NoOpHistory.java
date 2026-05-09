package info.henrycaldwell.aggregator.history;

import com.typesafe.config.Config;

import info.henrycaldwell.aggregator.config.Spec;

/**
 * Class for tracking clips by performing no action.
 *
 * This class accepts operations without recording any state to an underlying
 * store.
 */
public final class NoOpHistory extends AbstractHistory {

  public static final Spec SPEC = Spec.builder().build();

  /**
   * Constructs a NoOpHistory.
   *
   * @param config A {@link Config} representing the history configuration.
   */
  public NoOpHistory(Config config) {
    super(config, SPEC);
  }

  /**
   * Claims a clip without recording state.
   *
   * @param id     A string representing the clip identifier.
   * @param runner A string representing the runner name.
   * @return {@code true} always, as no state is recorded to detect duplicates.
   */
  @Override
  public boolean claim(String id, String runner) {
    return true;
  }

  /**
   * Marks a clip as successfully prepared without recording state.
   *
   * @param id     A string representing the clip identifier.
   * @param runner A string representing the runner name.
   */
  @Override
  public void prepare(String id, String runner) {
  }

  /**
   * Marks a clip as successfully published without recording state.
   *
   * @param id     A string representing the clip identifier.
   * @param runner A string representing the runner name.
   */
  @Override
  public void publish(String id, String runner) {
  }

  /**
   * Marks a clip as failed without recording state.
   *
   * @param id     A string representing the clip identifier.
   * @param runner A string representing the runner name.
   * @param error  A string representing the human-readable error message, or
   *               {@code null}.
   */
  @Override
  public void fail(String id, String runner, String error) {
  }
}
