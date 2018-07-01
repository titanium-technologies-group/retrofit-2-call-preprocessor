package codes.titanium.premapper.infra;

import retrofit2.http.GET;
import rx.Observable;

public interface TestService {
  @GET("/a")
  Observable<TestEntity> getEntity();

  @GET("/b")
  Observable<String> getString();
}
