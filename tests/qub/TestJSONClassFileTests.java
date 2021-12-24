package qub;

public interface TestJSONClassFileTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(TestJSONClassFile.class, () ->
        {
            runner.testGroup("create(JSONProperty)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> TestJSONClassFile.create((JSONProperty)null),
                        new PreConditionFailure("innerProperty cannot be null."));
                });

                runner.test("with non-null", (Test test) ->
                {
                    final TestJSONClassFile classFile = TestJSONClassFile.create(JSONProperty.create("a", "b"));
                    test.assertNotNull(classFile);
                    test.assertEqual("a", classFile.getFullTypeName());
                    test.assertEqual(JSONProperty.create("a", "b"), classFile.toJson());
                });
            });
        });
    }
}
