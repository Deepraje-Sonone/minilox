package com.minilox.interpreter;

public record RunLimits(
        int maxCallDepth,
        long maxSteps,
        long timeoutMillis,
        int maxOutputLines,
        int maxOutputChars,
        int maxStringLength,
        int maxSourceChars) {

    public static RunLimits unlimited() {
        return new RunLimits(
                200,
                Long.MAX_VALUE,
                Long.MAX_VALUE,
                Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                Integer.MAX_VALUE
        );
    }

    public static RunLimits playground() {
        return new RunLimits(
                200,
                2_000_000L,
                3_000L,
                500,
                20_000,
                100_000,
                10_000
        );
    }

    public RunLimits withMaxCallDepth(int value) {
        return new RunLimits(value, maxSteps, timeoutMillis,
                maxOutputLines, maxOutputChars, maxStringLength, maxSourceChars);
    }

    public RunLimits withMaxSteps(long value) {
        return new RunLimits(maxCallDepth, value, timeoutMillis,
                maxOutputLines, maxOutputChars, maxStringLength, maxSourceChars);
    }

    public RunLimits withTimeoutMillis(long value) {
        return new RunLimits(maxCallDepth, maxSteps, value,
                maxOutputLines, maxOutputChars, maxStringLength, maxSourceChars);
    }

    public RunLimits withMaxOutputLines(int value) {
        return new RunLimits(maxCallDepth, maxSteps, timeoutMillis,
                value, maxOutputChars, maxStringLength, maxSourceChars);
    }

    public RunLimits withMaxOutputChars(int value) {
        return new RunLimits(maxCallDepth, maxSteps, timeoutMillis,
                maxOutputLines, value, maxStringLength, maxSourceChars);
    }

    public RunLimits withMaxStringLength(int value) {
        return new RunLimits(maxCallDepth, maxSteps, timeoutMillis,
                maxOutputLines, maxOutputChars, value, maxSourceChars);
    }

    public RunLimits withMaxSourceChars(int value) {
        return new RunLimits(maxCallDepth, maxSteps, timeoutMillis,
                maxOutputLines, maxOutputChars, maxStringLength, value);
    }
}