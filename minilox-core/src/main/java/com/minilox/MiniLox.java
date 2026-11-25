
package com.minilox;

import com.minilox.error.MiniLoxException;
import com.minilox.lexer.Lexer;
import com.minilox.lexer.Token;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class MiniLox {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java com.minilox.MiniLox <script.mlox>");
            return;
        }

        try {
            String source = Files.readString(Path.of(args[0]));
            List<Token> tokens = new Lexer(source).scanTokens();

            for (Token token : tokens) {
                System.out.println(token);
            }

        } catch (IOException e) {
            System.err.println("Could not read file: " + e.getMessage());
            System.exit(66);

        } catch (MiniLoxException e) {
            System.err.println(e.report());
            System.exit(65);
        }
    }
}
