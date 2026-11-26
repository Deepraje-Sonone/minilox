# MiniLox Architecture

## 1. Overview

MiniLox is a custom programming language interpreter implemented in Java 21. It follows a modular architecture that separates language processing from the REST API.

The project has two main modules:

- **minilox-core:** Implements the MiniLox programming language and interpreter.
- **minilox-web:** Exposes interpreter functionality through a Spring Boot REST API.

The core module is independent of Spring Boot and can be used without the web application.

## 2. High-Level Architecture

A MiniLox program passes through the following stages:

```text
MiniLox Source Code
        |
        v
      Lexer
        |
        v
      Tokens
        |
        v
      Parser
        |
        v
       AST
        |
        v
   Interpreter
        |
        v
   Environment
        |
        v
   Program Output
```

## 3. Core Interpreter Components

### 3.1 Lexer

The lexer converts source code into a sequence of tokens.

Responsibilities:
- Recognize keywords, identifiers and operators.
- Identify literals such as numbers and strings.
- Track source line information.
- Collect lexical errors.

### 3.2 Parser

The parser converts tokens into an Abstract Syntax Tree (AST).

MiniLox uses recursive-descent parsing.

Responsibilities:
- Validate program syntax.
- Apply operator precedence.
- Construct expression and statement nodes.
- Report syntax errors.
- Desugar `for` loops into simpler statements.

### 3.3 Abstract Syntax Tree

The AST represents the structure of a MiniLox program.

It uses the Visitor pattern to separate operations on syntax nodes from the node definitions.

### 3.4 Interpreter

The interpreter evaluates AST expressions and executes statements.

Supported language features include:
- Arithmetic and logical expressions.
- Variables and assignment.
- Conditional statements and loops.
- Functions and return statements.
- Recursion and closures.
- Native `clock()` function.

### 3.5 Environment

The Environment class stores variables and their values.

It supports:
- Variable declaration.
- Variable lookup.
- Variable assignment.
- Nested scopes through linked environments.

### 3.6 Functions and Closures

MiniLox functions implement a callable abstraction.

Functions support parameters, arguments and return values. Closures retain access to the environment in which they were declared.

## 4. Execution and Error Handling

### 4.1 Runner

`MiniLoxRunner` provides a single entry point for executing source code.

It coordinates:
1. Lexical analysis.
2. Parsing.
3. Interpretation.
4. Error collection.
5. Result generation.

### 4.2 Result and Diagnostics

`RunResult` represents the outcome of program execution.

`Diagnostic` provides structured error information, including:
- Error category.
- Source line.
- Error message.

Supported diagnostic categories include syntax, runtime, limit and internal errors.

### 4.3 Execution Safety

`RunLimits` defines configurable resource limits.

The playground configuration restricts:
- Maximum call depth.
- Execution steps.
- Execution time.
- Output lines and characters.
- String length.
- Source code length.

These restrictions help prevent programs from consuming excessive resources.

## 5. REST API Architecture

The `minilox-web` module exposes the interpreter through HTTP endpoints.

```text
       HTTP Client
            |
            v
      RunController
            |
            v
        RunService
            |
            v
      MiniLoxRunner
            |
            v
       MiniLox Core
            |
            v
       RunResponse
            |
            v
       HTTP Response
```

### 5.1 RunController

`RunController` handles incoming HTTP requests.

Endpoints:

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/health` | Health check |
| POST | `/api/run` | Execute MiniLox source code |

### 5.2 RunService

`RunService` connects the REST API to the interpreter.

Responsibilities:
- Execute source code using `MiniLoxRunner`.
- Apply playground execution limits.
- Capture program output.
- Measure execution duration.
- Return a structured response.

### 5.3 Request and Response DTOs

`RunRequest` represents the incoming JSON request.

`RunResponse` represents the execution result, including success status, output, diagnostics and execution duration.

### 5.4 HTTP Error Handling

Empty or missing source code returns HTTP 400 Bad Request.

A syntactically invalid or unsuccessful MiniLox program returns an execution result with diagnostics. The API can therefore distinguish HTTP request validation from interpreter execution errors.

## 6. Testing Strategy

MiniLox uses JUnit 5 for automated testing.

The project includes:
- Unit tests for lexer, parser and interpreter.
- Tests for variables, scopes, functions and closures.
- Execution-limit tests.
- REST controller tests.
- Golden tests for example programs.
- Deterministic fuzz testing for robustness.

JaCoCo generates code coverage reports for the core module.

## 7. Technology Stack

- Java 21+
- Maven
- Spring Boot 4
- Spring MVC
- JUnit 5
- JaCoCo
- Git and GitHub

## 8. Design Decisions

- **Modular architecture:** Separates the interpreter from the web application.
- **Recursive-descent parser:** Keeps grammar implementation understandable.
- **Visitor pattern:** Separates AST structure from operations.
- **Environment chaining:** Supports nested scopes and closures.
- **Runner facade:** Provides a consistent execution interface.
- **Resource limits:** Restricts potentially unsafe program execution.
- **Automated testing:** Helps maintain correctness as the language grows.

## 9. Future Improvements

Possible future enhancements include:
- A browser-based code playground.
- Better source-code diagnostics.
- More language features.
- Additional integration and performance tests.
- Deployment of the REST API.
