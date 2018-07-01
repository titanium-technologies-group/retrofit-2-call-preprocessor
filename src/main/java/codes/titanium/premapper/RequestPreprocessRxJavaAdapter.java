package codes.titanium.premapper;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.Scheduler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter for preprocessing of entities received from server.
 * Contains map with {@link FlatMapper}, every time object of class that is stored inside {@link FlatMapper} received ->
 * it triggers corresponding flatmap
 */
public class RequestPreprocessRxJavaAdapter extends CallAdapter.Factory {

  private CallAdapter.Factory wrapped;

  private Map<Class, FlatMapper> flatMappersMap;

  public RequestPreprocessRxJavaAdapter(CallAdapter.Factory wrapped, List<FlatMapper> flatMappers) {
    if (flatMappers == null)
      throw new NullPointerException("Flatmappers should not be null");
    this.wrapped = wrapped;
    flatMappersMap = new HashMap<>();
    for (FlatMapper flatMapper : flatMappers)
      flatMappersMap.put(flatMapper.getResponseClass(), flatMapper);
  }

  /**
   * Returns an instance which creates synchronous observables that do not operate on any scheduler
   * by default.
   *
   * @param flatMappers that will intercept responses
   */
  public static RequestPreprocessRxJavaAdapter create(List<FlatMapper> flatMappers) {
    return new RequestPreprocessRxJavaAdapter(RxJavaCallAdapterFactory.create(), flatMappers);
  }

  /**
   * Returns an instance which creates asynchronous observables. Applying
   * {@link Observable#subscribeOn} has no effect on stream types created by this factory.
   *
   * @param flatMappers that will intercept responses
   */
  public static RequestPreprocessRxJavaAdapter createAsync(List<FlatMapper> flatMappers) {
    return new RequestPreprocessRxJavaAdapter(RxJavaCallAdapterFactory.createAsync(), flatMappers);
  }

  /**
   * Returns an instance which creates synchronous observables that
   * {@linkplain Observable#subscribeOn(Scheduler) subscribe on} {@code scheduler} by default.
   *
   * @param flatMappers that will intercept responses
   */
  @SuppressWarnings("ConstantConditions") // Guarding public API nullability.
  public static RequestPreprocessRxJavaAdapter createWithScheduler(Scheduler scheduler, List<FlatMapper> flatMappers) {
    return new RequestPreprocessRxJavaAdapter(RxJavaCallAdapterFactory.createWithScheduler(scheduler), flatMappers);
  }

  @Override
  public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
    CallAdapter<?, ?> result = wrapped.get(returnType, annotations, retrofit);
    if (result != null && getRawType(returnType) == Observable.class && flatMappersMap.containsKey(result.responseType()))
      return new PremapperCallAdapter(result, flatMappersMap.get(result.responseType()));
    return result;
  }

  private static class PremapperCallAdapter<R> implements CallAdapter<R, Object> {

    private CallAdapter<R, Object> wrapped;
    private FlatMapper flatMapper;

    public PremapperCallAdapter(CallAdapter<R, Object> wrapped, FlatMapper flatMapper) {
      this.wrapped = wrapped;
      this.flatMapper = flatMapper;
    }

    @Override
    public Type responseType() {
      return wrapped.responseType();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object adapt(Call<R> call) {
      return flatMapper.flatMapInto((Observable) wrapped.adapt(call));
    }
  }

}
