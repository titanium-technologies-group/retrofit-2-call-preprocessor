package codes.titanium.premapper.infra;

public class ParametrizedTestEntity<T, E> {
  private T t;
  private E e;

  public T getT() {
    return t;
  }

  public ParametrizedTestEntity<T, E> setT(T t) {
    this.t = t;
    return this;
  }

  public E getE() {
    return e;
  }

  public ParametrizedTestEntity<T, E> setE(E e) {
    this.e = e;
    return this;
  }
}
