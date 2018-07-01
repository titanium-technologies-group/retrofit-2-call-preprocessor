package codes.titanium.premapper;

import codes.titanium.premapper.infra.*;
import okhttp3.OkHttpClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.plugins.RxJavaHooks;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class Tests {

  private TestService testService;
  private HttpTestInterceptor interceptor;

  @Before
  public void setUp() throws Exception {
    RxJavaHooks.setOnIOScheduler(scheduler -> Schedulers.immediate());
    interceptor = new HttpTestInterceptor();
  }

  private void initRetrofit(List<FlatMapper> flatMappers) {
    OkHttpClient client = new OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build();
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("http://example.com")
        .client(client)
        .addConverterFactory(new TestEntityConverter())
        .addConverterFactory(new StringConverter())
        .addCallAdapterFactory(RequestPreprocessRxJavaAdapter.createWithScheduler(Schedulers.io(), flatMappers))
        .build();
    testService = retrofit.create(TestService.class);
  }

  @Test
  public void workingProperlyWithFlatmapper() throws Exception {
    String name = "testName";
    FlatMapper<TestEntity> testEntityFlatMapper = createFlatmapperForClass(TestEntity.class,
        testEntityObservable -> testEntityObservable.doOnNext(testEntity -> testEntity.setTestString(name)));
    initRetrofit(Collections.singletonList(testEntityFlatMapper));
    interceptor.addToQueue(200, "");
    TestSubscriber<TestEntity> subscriber = new TestSubscriber<>();
    testService.getEntity().subscribe(subscriber);
    TestEntity testEntity = subscriber.getOnNextEvents().get(0);
    Assert.assertSame(name, testEntity.getTestString());
    subscriber.assertCompleted();
    subscriber.assertNoErrors();
  }

  @Test
  public void onErrorLogicWorkingProperly() throws Exception {
    String name = "testName";
    FlatMapper<TestEntity> testEntityFlatMapper = createFlatmapperForClass(TestEntity.class,
        testEntityObservable -> testEntityObservable.map(testEntity -> testEntity.setTestString(name)));
    initRetrofit(Collections.singletonList(testEntityFlatMapper));
    interceptor.addToQueue(400, "");
    TestSubscriber<TestEntity> subscriber = new TestSubscriber<>();
    testService.getEntity().subscribe(subscriber);
    subscriber.assertError(HttpException.class);
  }

  @Test
  public void workingProperlyWithoutFlatmapper() throws Exception {
    initRetrofit(new ArrayList<>());
    interceptor.addToQueue(200, "");
    TestSubscriber<TestEntity> subscriber = new TestSubscriber<>();
    testService.getEntity().subscribe(subscriber);
    TestEntity testEntity = subscriber.getOnNextEvents().get(0);
    Assert.assertSame(null, testEntity.getTestString());
    subscriber.assertCompleted();
    subscriber.assertNoErrors();
  }

  private <T> FlatMapper<T> createFlatmapperForClass(Class<T> tClass, Function<Observable<T>, Observable<T>> consumer) {
    return new FlatMapper<T>() {
      @Override
      public Class<T> getResponseClass() {
        return tClass;
      }

      @Override
      public Observable<T> flatMapInto(Observable<T> source) {
        return consumer.apply(source);
      }
    };
  }

}
