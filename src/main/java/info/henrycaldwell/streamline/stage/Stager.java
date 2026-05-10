package info.henrycaldwell.streamline.stage;

import info.henrycaldwell.streamline.core.MediaRef;

/**
 * Interface for staging media.
 * 
 * This interface defines a contract for producing remote media from local
 * media.
 */
public interface Stager {

  /**
   * Initializes any underlying resources required by the stager.
   */
  void start();

  /**
   * Releases any resources acquired by {@link #start()}.
   */
  void stop();

  /**
   * Returns the configured stager name.
   *
   * @return A string representing the stager name.
   */
  String getName();

  /**
   * Stages the input media to a remote location.
   *
   * @param media A {@link MediaRef} representing the media to stage.
   * @return A {@link MediaRef} representing the staged media.
   */
  MediaRef stage(MediaRef media);

  /**
   * Cleans staged resources associated with the media.
   *
   * @param media A {@link MediaRef} representing the staged media.
   */
  void clean(MediaRef media);

  /**
   * Purges all staged resources.
   */
  void purge();
}
