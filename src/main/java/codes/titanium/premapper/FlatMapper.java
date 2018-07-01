package codes.titanium.premapper;

import rx.Observable;

/**
 * Main interface for flatmapper.
 * Fill {@link FlatMapper#flatMapInto(Observable)} with logic to intercept objects before you recieve them on call.
 */
public interface FlatMapper<T> {

  /**
   * Class you would like to intercept
   */
  Class<T> getResponseClass();

  /**
   * Main logic of flatmap should be here.
   * Object is intercepted here just after it was received from server.
   *
   * @param source observable with object just after they were received
   */
  Observable<T> flatMapInto(Observable<T> source);
}
