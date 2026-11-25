package com.minilox;

import com.minilox.ast.AstPrinter;
import com.minilox.ast.Stmt;
import com.minilox.error.MiniLoxException;
import com.minilox.error.ParseException;
import com.minilox.lexer.Lexer;
import com.minilox.lexer.Token;
import com.minilox.parser.Parser;

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

            Parser parser = new Parser(tokens);
            List<Stmt> statements = parser.parse();

            if (!parser.getErrors().isEmpty()) {
                for (ParseException error : parser.getErrors()) {
                    System.err.println(error.report());
                }
                System.exit(65);
            }

            System.out.println(new AstPrinter().printProgram(statements));
        } catch (IOException e) {
            System.err.println("Could not read file: " + e.getMessage());
            System.exit(66);
        } catch (MiniLoxException e) {
            System.err.println(e.report());
            System.exit(65);
        }
    }
}