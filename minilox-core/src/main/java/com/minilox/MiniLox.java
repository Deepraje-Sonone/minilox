
package com.minilox;

import com.minilox.error.Diagnostic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class MiniLox {

    public static void main(String[] args) {
        boolean printAst = false;
        String path = null;

        for (String arg : args) {
            if (arg.equals("--ast")) {
                printAst = true;
            } else {
                path = arg;
            }
        }

        if (path == null) {
            System.out.println(
                    "Usage: java com.minilox.MiniLox [--ast] <script.mlox>");
            return;
        }

        String source;

        try {
            source = Files.readString(Path.of(path));
        } catch (IOException e) {
            System.err.println("Could not read file: " + e.getMessage());
            System.exit(66);
            return;
        }

        MiniLoxRunner runner = new MiniLoxRunner();
        Consumer<String> output = System.out::println;

        RunResult result;

        if (printAst) {
            result = runner.dumpAst(source, output);
        } else {
            result = runner.run(source, output);
        }

        for (Diagnostic error : result.errors()) {
            System.err.println(error.format());
        }

        if (!result.ok()) {
            System.exit(result.exitCode());
        }
    }
}
