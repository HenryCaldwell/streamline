package info.henrycaldwell.streamline.publish;

import java.net.http.HttpRequest;

/**
 * Functional interface for sending an HTTP request.
 *
 * This interface defines a contract for dispatching HTTP requests used by
 * publishers, allowing real HTTP calls to be substituted in tests.
 */
@FunctionalInterface
interface HttpSender {

  /**
   * Sends an HTTP request and returns the response body.
   *
   * @param request A {@link HttpRequest} representing the request to send.
   * @return A string representing the response body.
   */
  String send(HttpRequest request);
}
