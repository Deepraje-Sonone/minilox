
package com.minilox;

import com.minilox.interpreter.RunLimits;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class ExamplesGoldenTest {

    private static final Path EXAMPLES_DIR = Path.of("..", "examples");

    @TestFactory
    Stream<DynamicTest> everyExampleProducesItsExpectedOutput()
            throws IOException {

        assertTrue(
                Files.isDirectory(EXAMPLES_DIR),
                "Examples folder not found: " + EXAMPLES_DIR.toAbsolutePath()
        );

        List<Path> programs;

        try (Stream<Path> files = Files.list(EXAMPLES_DIR)) {
            programs = files
                    .filter(p -> p.getFileName().toString().endsWith(".mlox"))
                    .sorted()
                    .toList();
        }

        return programs.stream().map(program ->
                dynamicTest(
                        program.getFileName().toString(),
                        () -> check(program)
                )
        );
    }

    private void check(Path program) throws IOException {

        String actual = runAndCollect(
                Files.readString(program, StandardCharsets.UTF_8)
        );

        Path expectedFile = expectedFileFor(program);

        if (System.getenv("MINILOX_BLESS") != null) {
            Files.writeString(
                    expectedFile,
                    actual + "\n",
                    StandardCharsets.UTF_8
            );
            return;
        }

        assumeTrue(
                Files.exists(expectedFile),
                "No expected-output file for " + program.getFileName()
        );

        String expected = Files.readString(
                expectedFile,
                StandardCharsets.UTF_8
        );

        assertEquals(normalize(expected), normalize(actual));
    }

    private String runAndCollect(String source) {

        List<String> lines = new ArrayList<>();

        RunLimits safety = RunLimits.unlimited()
                .withMaxSteps(5_000_000)
                .withTimeoutMillis(10_000);

        RunResult result = new MiniLoxRunner(safety)
                .run(source, lines::add);

        result.errors().forEach(error -> lines.add(error.format()));

        return String.join("\n", lines);
    }

    private Path expectedFileFor(Path program) {

        String name = program.getFileName().toString();

        String base = name.substring(
                0,
                name.length() - ".mlox".length()
        );

        return program.resolveSibling(base + ".expected");
    }

    private String normalize(String text) {
        return text.replace("\r\n", "\n").stripTrailing();
    }
}
