package codes.titanium.premapper.infra;

import okhttp3.*;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

public class HttpTestInterceptor implements Interceptor {
  private Queue<Response> responsesQueue = new ArrayDeque<>();
  private int executedRequestsCount;

  @Override
  public Response intercept(Chain chain) throws IOException {
    executedRequestsCount++;
    return responsesQueue.poll();
  }

  /**
   * Adds pending request to queue
   *
   * @param code response code
   * @param body request body to return to user
   */
  public void addToQueue(int code, String body) {
    addToQueue(code, body, "Success");
  }

  /**
   * Adds pending request to queue
   *
   * @param code    response code
   * @param body    request body to return to user
   * @param message HTTP status message
   */
  public void addToQueue(int code, String body, String message) {
    ResponseBody responseBody = ResponseBody.create(MediaType.parse("text/plain; charset=utf-8"), body);
    responsesQueue.add(new Response.Builder()
        .request(new Request.Builder().url("http://f8.ai").build())
        .protocol(Protocol.HTTP_2)
        .body(responseBody)
        .message(message)
        .code(code)
        .build());
  }

  /**
   * Clears queue with responses and executed requests count
   */
  public void clearQueue() {
    responsesQueue.clear();
    executedRequestsCount = 0;
  }

  /**
   * @return number of executed requests by this test interceptor
   */
  public int getExecutedRequestsCount() {
    return executedRequestsCount;
  }
}