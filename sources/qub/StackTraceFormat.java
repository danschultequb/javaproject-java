package qub;

public class StackTraceFormat
{
    private final Set<String> fullyQualifiedTypeNamesToIgnore;

    private StackTraceFormat()
    {
        this.fullyQualifiedTypeNamesToIgnore = Set.create();
    }

    public static StackTraceFormat create()
    {
        return new StackTraceFormat();
    }

    /**
     * Ignore any {@link StackTraceElement}s that belong to the provided type.
     * @param type The type that will be added to the ignore list.
     * @return This object for method chaining.
     */
    public StackTraceFormat ignoreType(Class<?> type)
    {
        PreCondition.assertNotNull(type, "type");

        return this.ignoreType(Types.getFullTypeName(type));
    }

    /**
     * Ignore any {@link StackTraceElement}s that belong to the provided type.
     * @param fullyQualifiedTypeName The type that will be added to the ignore list.
     * @return This object for method chaining.
     */
    public StackTraceFormat ignoreType(String fullyQualifiedTypeName)
    {
        PreCondition.assertNotNullAndNotEmpty(fullyQualifiedTypeName, "fullyQualifiedTypeName");

        this.fullyQualifiedTypeNamesToIgnore.add(fullyQualifiedTypeName);

        return this;
    }

    /**
     * Get whether this {@link StackTraceElement} should be shown.
     * @param element The {@link StackTraceElement} to check.
     * @return Whether this {@link StackTraceElement} should be shown.
     */
    public boolean shouldShow(StackTraceElement element)
    {
        PreCondition.assertNotNull(element, "element");

        return !this.fullyQualifiedTypeNamesToIgnore.contains(element.getClassName());
    }
}
