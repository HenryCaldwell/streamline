package info.henrycaldwell.aggregator.download;

import java.nio.file.Path;

import com.typesafe.config.Config;

import info.henrycaldwell.aggregator.config.Spec;
import info.henrycaldwell.aggregator.core.ClipRef;
import info.henrycaldwell.aggregator.core.MediaRef;

/**
 * Class for downloading clips by performing no action.
 *
 * This class accepts a clip without downloading any file.
 */
public final class NoOpDownloader extends AbstractDownloader {

  public static final Spec SPEC = Spec.builder().build();

  /**
   * Constructs a NoOpDownloader.
   *
   * @param config A {@link Config} representing the downloader configuration.
   */
  public NoOpDownloader(Config config) {
    super(config, SPEC);
  }

  /**
   * Downloads the input clip by performing no action.
   *
   * @param clip   A {@link ClipRef} representing the clip to download.
   * @param target A {@link Path} representing the media destination.
   * @return A {@link MediaRef} representing the downloaded media.
   */
  @Override
  public MediaRef download(ClipRef clip, Path target) {
    return new MediaRef(clip.id(), target, null, clip.title(), clip.broadcaster(), clip.language(), clip.tags());
  }
}
