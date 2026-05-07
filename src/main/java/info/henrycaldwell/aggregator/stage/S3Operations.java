package info.henrycaldwell.aggregator.stage;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Interface for performing S3-compatible object storage operations.
 *
 * This interface defines a contract for dispatching S3 operations used by
 * stagers, allowing real S3 calls to be substituted in tests.
 */
interface S3Operations {

  /**
   * Uploads an object to a bucket.
   *
   * @param request A {@link PutObjectRequest} representing the upload request.
   * @param body    A {@link RequestBody} representing the object content.
   */
  void putObject(PutObjectRequest request, RequestBody body);

  /**
   * Deletes an object from a bucket.
   *
   * @param request A {@link DeleteObjectRequest} representing the delete request.
   */
  void deleteObject(DeleteObjectRequest request);
}
