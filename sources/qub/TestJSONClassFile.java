package qub;

public class TestJSONClassFile extends JSONPropertyWrapperBase
{
    private static final String lastModifiedPropertyName = "lastModified";
    private static final String passedTestCountPropertyName = "passedTestCount";
    private static final String failedTestCountPropertyName = "failedTestCount";
    private static final String skippedTestCountPropertyName = "skippedTestCount";

    private TestJSONClassFile(JSONProperty innerProperty)
    {
        super(innerProperty);
    }

    public static TestJSONClassFile create(JSONProperty innerProperty)
    {
        return new TestJSONClassFile(innerProperty);
    }

    public static TestJSONClassFile create(String testClassRelativePath)
    {
        PreCondition.assertNotNullAndNotEmpty(testClassRelativePath, "testClassRelativePath");

        return TestJSONClassFile.create(Path.parse(testClassRelativePath));
    }

    public static TestJSONClassFile create(Path testClassRelativePath)
    {
        PreCondition.assertNotNull(testClassRelativePath, "testClassRelativePath");
        PreCondition.assertFalse(testClassRelativePath.isRooted(), "testClassRelativePath.isRooted()");

        return TestJSONClassFile.create(JSONProperty.create(testClassRelativePath.toString(), JSONObject.create()));
    }

    public Path getRelativePath()
    {
        return Path.parse(this.toJson().getName());
    }

    public String getFullTypeName()
    {
        return JavaFile.getFullTypeName(this.getRelativePath());
    }

    private JSONObject getValue()
    {
        return this.toJson().getObjectValue().catchError().await();
    }

    private String getStringValue(String propertyName)
    {
        PreCondition.assertNotNullAndNotEmpty(propertyName, "propertyName");

        String result = null;

        final JSONObject jsonValue = this.getValue();
        if (jsonValue != null)
        {
            result = jsonValue.getString(propertyName).catchError().await();
        }

        return result;
    }

    private TestJSONClassFile setStringValue(String propertyName, String propertyValue)
    {
        PreCondition.assertNotNullAndNotEmpty(propertyName, "propertyName");
        PreCondition.assertNotNull(propertyValue, "propertyValue");

        final JSONObject jsonValue = this.getValue();
        if (jsonValue != null)
        {
            jsonValue.setString(propertyName, propertyValue);
        }

        return this;
    }

    private Integer getIntegerValue(String propertyName)
    {
        PreCondition.assertNotNullAndNotEmpty(propertyName, "propertyName");

        Integer result = null;

        final JSONObject jsonValue = this.getValue();
        if (jsonValue != null)
        {
            result = jsonValue.getInteger(propertyName).catchError().await();
        }

        return result;
    }

    private TestJSONClassFile setIntegerValue(String propertyName, int propertyValue)
    {
        PreCondition.assertNotNullAndNotEmpty(propertyName, "propertyName");

        final JSONObject jsonValue = this.getValue();
        if (jsonValue != null)
        {
            jsonValue.setNumber(propertyName, propertyValue);
        }

        return this;
    }

    public DateTime getLastModified()
    {
        final String lastModifiedString = this.getStringValue(TestJSONClassFile.lastModifiedPropertyName);
        return Strings.isNullOrEmpty(lastModifiedString)
            ? null
            : DateTime.parse(lastModifiedString).catchError().await();
    }

    public TestJSONClassFile setLastModified(DateTime lastModified)
    {
        PreCondition.assertNotNull(lastModified, "lastModified");

        return this.setStringValue(TestJSONClassFile.lastModifiedPropertyName, lastModified.toString());
    }

    public Integer getPassedTestCount()
    {
        return this.getIntegerValue(TestJSONClassFile.passedTestCountPropertyName);
    }

    public TestJSONClassFile setPassedTestCount(int passedTestCount)
    {
        PreCondition.assertGreaterThanOrEqualTo(passedTestCount, 0, "passedTestCount");

        return this.setIntegerValue(TestJSONClassFile.passedTestCountPropertyName, passedTestCount);
    }

    public Integer getFailedTestCount()
    {
        return this.getIntegerValue(TestJSONClassFile.failedTestCountPropertyName);
    }

    public TestJSONClassFile setFailedTestCount(int failedTestCount)
    {
        PreCondition.assertGreaterThanOrEqualTo(failedTestCount, 0, "failedTestCount");

        return this.setIntegerValue(TestJSONClassFile.failedTestCountPropertyName, failedTestCount);
    }

    public Integer getSkippedTestCount()
    {
        return this.getIntegerValue(TestJSONClassFile.skippedTestCountPropertyName);
    }

    public TestJSONClassFile setSkippedTestCount(int skippedTestCount)
    {
        PreCondition.assertGreaterThanOrEqualTo(skippedTestCount, 0, "skippedTestCount");

        return this.setIntegerValue(TestJSONClassFile.skippedTestCountPropertyName, skippedTestCount);
    }
}
