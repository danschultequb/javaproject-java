package qub;

public class TestJSONClassFile extends JSONPropertyWrapperBase
{
    private TestJSONClassFile(JSONProperty innerProperty)
    {
        super(innerProperty);
    }

    public static TestJSONClassFile create(JSONProperty innerProperty)
    {
        return new TestJSONClassFile(innerProperty);
    }

    public String getFullTypeName()
    {
        return this.toJson().getName();
    }
}
