package qub;

/**
 * The type of code coverage information that will be collected during a test run.
 */
public enum Coverage
{
    /**
     * No code coverage information will be collected.
     */
    None,
    /**
     * Code coverage will be collected for source code files.
     */
    Sources,
    /**
     * Code coverage will be collected for test code files.
     */
    Tests,
    /**
     * Code coverage will be collected for all code files.
     */
    All,
}
