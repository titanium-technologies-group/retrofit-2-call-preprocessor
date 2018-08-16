package codes.titanium.premapper;


/**
 * Main interface for preprocessor
 * Fill {@link Preprocessor#preprocess(Object)} with logic to intercept objects before you receive them on call.
 */
public interface Preprocessor<T> {

  /**
   * Main logic of flatmap should be here.
   * Object is intercepted here just after it was received from server.
   *
   * @param source observable with object just after they were received
   */
  T preprocess(T source);

}
