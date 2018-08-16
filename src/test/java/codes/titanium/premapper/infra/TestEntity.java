package codes.titanium.premapper.infra;

public class TestEntity {
  private String testString;
  private int testInt;

  public String getTestString() {
    return testString;
  }

  public TestEntity setTestString(String testString) {
    this.testString = testString;
    return this;
  }

  public TestEntity setTestInt(int testInt) {
    this.testInt = testInt;
    return this;
  }

  public int getTestInt() {
    return testInt;
  }
}
