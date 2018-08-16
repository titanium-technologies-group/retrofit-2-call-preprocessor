package codes.titanium.premapper.infra;

import codes.titanium.premapper.PreprocessIgnore;
import retrofit2.http.GET;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TestService {
  @GET("/a")
  Observable<TestEntity> getEntity();

  @GET("/b")
  Observable<List<ParametrizedTestEntity<Map<ArrayList, Set>, String>>> getParametrized();

  @GET("/c")
  Observable<List<TestEntity>> getTestEntities();

  @GET("/g")
  @PreprocessIgnore
  Observable<TestEntity> getEntityIgnored();

}
