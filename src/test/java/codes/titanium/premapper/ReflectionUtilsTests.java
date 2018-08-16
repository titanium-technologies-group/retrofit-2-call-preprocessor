package codes.titanium.premapper;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import rx.Observable;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static codes.titanium.premapper.ReflectionUtils.*;
import static org.junit.Assert.*;

public class ReflectionUtilsTests {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void containsAnnotationWorksProperly() throws Exception {
    Annotation[] annotations = {() -> Test.class, () -> Override.class};
    Assert.assertTrue(containsAnnotation(annotations, Override.class));
    Assert.assertFalse(containsAnnotation(annotations, PreprocessIgnore.class));
  }

  @Test
  public void notWorkingForLambdas() throws Exception {
    Preprocessor<String> lambdaPreprocessor = source -> source;
    thrown.expect(IllegalStateException.class);
    getFirstNonSyntheticMethodForName(lambdaPreprocessor, "preprocess");
  }

  @Test
  public void workingCorrectlyForNonLambdas() throws Exception {
    Preprocessor<String> stringPreprocessor = new Preprocessor<String>() {
      @Override
      public String preprocess(String source) {
        return source;
      }
    };
    assertSame(getFirstNonSyntheticMethodForName(stringPreprocessor, "preprocess").getGenericReturnType(), String.class);
  }

  @Test
  public void assignsSameObjects() throws Exception {
    Type mapListStingType0 = extractReturnTypeFromTestClassByName("mapListString");
    Type mapListStingType1 = extractReturnTypeFromTestClassByName("sameMapListString");
    assertEquals(mapListStingType0, mapListStingType1);
    assertTrue(isAssignableFromTo(mapListStingType0, mapListStingType1));
  }

  @Test
  public void assignsToFullWildCard() throws Exception {
    Type fullWildWard = extractReturnTypeFromTestClassByName("fullWildCard");
    Type object = extractReturnTypeFromTestClassByName("object");
    Type serializableWildCard = extractReturnTypeFromTestClassByName("serializableWildCard");
    Type mapListString = extractReturnTypeFromTestClassByName("mapListString");
    Type mapWildcardListWildcardObject = extractReturnTypeFromTestClassByName("mapWildcardListWildcardObject");
    assertTrue(isAssignableFromTo(object, fullWildWard));
    assertTrue(isAssignableFromTo(serializableWildCard, fullWildWard));
    assertTrue(isAssignableFromTo(mapListString, fullWildWard));
    assertTrue(isAssignableFromTo(mapWildcardListWildcardObject, fullWildWard));
  }

  @Test
  public void emptyNotAssignsToTyped() throws Exception {
    Type object = extractReturnTypeFromTestClassByName("object");
    Type serializableWildCard = extractReturnTypeFromTestClassByName("serializableWildCard");
    assertFalse(isAssignableFromTo(object, serializableWildCard));
  }

  @Test
  public void assignsCorrectlyToSpecificWildCard() throws Exception {
    Type mapListString = extractReturnTypeFromTestClassByName("mapListString");
    Type mapWildcardListWildcardObject = extractReturnTypeFromTestClassByName("mapWildcardListWildcardObject");
    assertTrue(isAssignableFromTo(mapListString, mapWildcardListWildcardObject));
  }

  @Test
  public void notAssignsWhenNotNeeded() throws Exception {
    Type mapSetString = extractReturnTypeFromTestClassByName("mapSetString");
    Type mapWildcardListWildcardObject = extractReturnTypeFromTestClassByName("mapWildcardListWildcardObject");
    assertFalse(isAssignableFromTo(mapSetString, mapWildcardListWildcardObject));
  }

  @Test
  public void parametrizedTypesWorksProperly() throws Exception {
    Type listString = extractReturnTypeFromTestClassByName("listString");
    Type listObject = extractReturnTypeFromTestClassByName("listObject");
    Type listInteger = extractReturnTypeFromTestClassByName("listInteger");
    Type listNumber = extractReturnTypeFromTestClassByName("listNumber");
    assertFalse(isAssignableFromTo(listInteger, listObject));
    assertFalse(isAssignableFromTo(listNumber, listObject));
    assertFalse(isAssignableFromTo(listString, listObject));
    assertFalse(isAssignableFromTo(listInteger, listNumber));
    assertTrue(isAssignableFromTo(listInteger, listInteger));
  }

  @Test
  public void differentRawTypesWithSameParamsAreNotAssignable() throws Exception {
    Type listString = extractReturnTypeFromTestClassByName("listString");
    Type setString = extractReturnTypeFromTestClassByName("setString");
    assertFalse(isAssignableFromTo(listString, setString));
  }

  @Test
  public void nestedWildCardIsAssigned() throws Exception {
    Type listString = extractReturnTypeFromTestClassByName("listString");
    Type wildListWildSet = extractReturnTypeFromTestClassByName("wildListWildSet");
    Type listSet = extractReturnTypeFromTestClassByName("listSet");
    assertTrue(isAssignableFromTo(listSet, wildListWildSet));
    assertFalse(isAssignableFromTo(listString, wildListWildSet));
  }

  private static abstract class TestClass {

    abstract Observable<?> fullWildCard();

    abstract Observable<List<? extends Serializable>> serializableWildCard();

    abstract Observable<? extends List<? extends Set>> wildListWildSet();

    abstract Observable<List<HashSet>> listSet();

    abstract Observable<List<String>> listString();

    abstract Observable<Set<String>> setString();

    abstract Observable<List<Object>> listObject();

    abstract Observable<List<Integer>> listInteger();

    abstract Observable<List<Number>> listNumber();

    abstract Observable<Map<List, String>> mapListString();

    abstract Observable<Map<List, String>> sameMapListString();

    abstract Observable<Map<Set, String>> mapSetString();

    abstract Observable<Map<? extends List, ?>> mapWildcardListWildcardObject();

    abstract Observable object();

  }

  private Type extractReturnTypeFromTestClassByName(String name) throws Exception {
    return TestClass.class.getDeclaredMethod(name).getGenericReturnType();
  }

}