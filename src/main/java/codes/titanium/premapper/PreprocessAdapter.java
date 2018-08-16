package codes.titanium.premapper;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static codes.titanium.premapper.ReflectionUtils.*;

/**
 * Adapter for preprocessing of entities received from server.
 * Contains map with {@link Preprocessor}, every time object of class that is stored inside {@link Preprocessor} received ->
 * it triggers corresponding preprocessor
 */
public class PreprocessAdapter extends CallAdapter.Factory {

  private CallAdapter.Factory wrapped;

  private List<Preprocessor> preprocessors;

  private PreprocessAdapter(CallAdapter.Factory wrapped, List<Preprocessor> preprocessors) {
    if (preprocessors == null)
      throw new NullPointerException("Preprocessors should not be null");
    this.wrapped = wrapped;
    this.preprocessors = new ArrayList<>(preprocessors);
  }

  /**
   * Creates new instance of preprocess adapter bases on your selected adapter
   *
   * @param preprocessors that will intercept responses
   */
  public static PreprocessAdapter create(CallAdapter.Factory factory, List<Preprocessor> preprocessors) {
    return new PreprocessAdapter(factory, preprocessors);
  }

  @Override
  public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
    CallAdapter<?, ?> result = wrapped.get(returnType, annotations, retrofit);
    List<Preprocessor> preprocessors = getNeededPreprocessors(returnType);
    if (result != null && !preprocessors.isEmpty() && !containsAnnotation(annotations, PreprocessIgnore.class))
      return new PremapperCallAdapter(result, preprocessors);
    return result;
  }

  private List<Preprocessor> getNeededPreprocessors(Type returnType) {
    List<Preprocessor> result = new ArrayList<>();
    for (Preprocessor preprocessor : preprocessors) {
      if (isAssignableFromTo(returnType,getFirstNonSyntheticMethodForName(preprocessor, "preprocess").getGenericReturnType()))
        result.add(preprocessor);
    }
    return result;
  }

  private static class PremapperCallAdapter<R> implements CallAdapter<R, Object> {

    private CallAdapter<R, Object> wrapped;
    private List<Preprocessor> preprocessors;

    private PremapperCallAdapter(CallAdapter<R, Object> wrapped, List<Preprocessor> preprocessors) {
      this.wrapped = wrapped;
      this.preprocessors = preprocessors;
    }

    @Override
    public Type responseType() {
      return wrapped.responseType();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object adapt(Call<R> call) {
      Object result = wrapped.adapt(call);
      for (Preprocessor preprocessor : preprocessors) {
        result = preprocessor.preprocess(result);
      }
      return result;
    }
  }

}
