package codes.titanium.premapper;

import codes.titanium.premapper.infra.*;
import okhttp3.OkHttpClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.plugins.RxJavaHooks;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Tests {

  private TestService testService;
  private HttpTestInterceptor interceptor;

  @Before
  public void setUp() throws Exception {
    RxJavaHooks.setOnIOScheduler(scheduler -> Schedulers.immediate());
    interceptor = new HttpTestInterceptor();
  }

  private void initRetrofit(List<Preprocessor> preprocessors) {
    OkHttpClient client = new OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build();
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("http://example.com")
        .client(client)
        .addConverterFactory(new TestEntityConverter())
        .addConverterFactory(new StringConverter())
        .addCallAdapterFactory(PreprocessAdapter.create(RxJavaCallAdapterFactory.create(), preprocessors))
        .build();
    testService = retrofit.create(TestService.class);
  }

  @Test
  public void workingProperlyWithPreprocessor() throws Exception {
    String name = "testName";
    Preprocessor<Observable<TestEntity>> testEntityPreprocessor = createDefaultTestEntityPreprocessor(name);
    initRetrofit(Collections.singletonList(testEntityPreprocessor));
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
    Preprocessor<Observable<TestEntity>> testEntityPreprocessor = createDefaultTestEntityPreprocessor(name);
    initRetrofit(Collections.singletonList(testEntityPreprocessor));
    interceptor.addToQueue(400, "");
    TestSubscriber<TestEntity> subscriber = new TestSubscriber<>();
    testService.getEntity().subscribe(subscriber);
    subscriber.assertError(HttpException.class);
  }

  @Test
  public void retriesInCaseOfFail() throws Exception {
    String name = "testName";
    Preprocessor<Observable<TestEntity>> testEntityPreprocessor = createDefaultTestEntityPreprocessor(name);
    Preprocessor<Observable<?>> generalPreprocessor = new Preprocessor<Observable<?>>() {
      @Override
      public Observable<?> preprocess(Observable<?> source) {
        return source.retry(1);
      }
    };
    initRetrofit(Arrays.asList(generalPreprocessor, testEntityPreprocessor));
    interceptor.addToQueue(400, "");
    interceptor.addToQueue(200, "");
    TestSubscriber<TestEntity> subscriber = new TestSubscriber<>();
    testService.getEntity().subscribe(subscriber);
    TestEntity testEntity = subscriber.getOnNextEvents().get(0);
    Assert.assertSame(name, testEntity.getTestString());
    subscriber.assertCompleted();
    subscriber.assertNoErrors();
  }

  @Test
  public void ignoringCorrectly() throws Exception {
    String name = "testName";
    Preprocessor<Observable<TestEntity>> testEntityPreprocessor = createDefaultTestEntityPreprocessor(name);
    initRetrofit(Collections.singletonList(testEntityPreprocessor));
    interceptor.addToQueue(200, "");
    TestSubscriber<TestEntity> subscriber = new TestSubscriber<>();
    testService.getEntityIgnored().subscribe(subscriber);
    TestEntity testEntity = subscriber.getOnNextEvents().get(0);
    Assert.assertSame(null, testEntity.getTestString());
    subscriber.assertCompleted();
    subscriber.assertNoErrors();
  }

  @Test
  public void twoPreprocessorsWorksCorrectly() throws Exception {
    String name = "testName";
    int testInt = 10;
    Preprocessor<Observable<TestEntity>> testEntityPreprocessor = createDefaultTestEntityPreprocessor(name);
    Preprocessor<Observable<? extends TestEntity>> universalPreprocessor = new Preprocessor<Observable<? extends TestEntity>>() {
      @Override
      public Observable<? extends TestEntity> preprocess(Observable<? extends TestEntity> source) {
        return source.doOnNext(o -> o.setTestInt(testInt));
      }
    };
    initRetrofit(Arrays.asList(testEntityPreprocessor, universalPreprocessor));
    interceptor.addToQueue(200, "");
    TestSubscriber<TestEntity> subscriber = new TestSubscriber<>();
    testService.getEntity().subscribe(subscriber);
    TestEntity testEntity = subscriber.getOnNextEvents().get(0);
    Assert.assertSame(name, testEntity.getTestString());
    Assert.assertSame(testInt, testEntity.getTestInt());
    subscriber.assertCompleted();
    subscriber.assertNoErrors();
  }

  @Test
  public void noPreprocessorDoesNotChangesAnything() throws Exception {
    initRetrofit(new ArrayList<>());
    interceptor.addToQueue(200, "");
    TestSubscriber<TestEntity> subscriber = new TestSubscriber<>();
    testService.getEntity().subscribe(subscriber);
    TestEntity testEntity = subscriber.getOnNextEvents().get(0);
    Assert.assertSame(null, testEntity.getTestString());
    subscriber.assertCompleted();
    subscriber.assertNoErrors();
  }

  private Preprocessor<Observable<TestEntity>> createDefaultTestEntityPreprocessor(String testName) {
    return new Preprocessor<Observable<TestEntity>>() {
      @Override
      public Observable<TestEntity> preprocess(Observable<TestEntity> source) {
        return source.doOnNext(testEntity -> testEntity.setTestString(testName));
      }
    };
  }

}
