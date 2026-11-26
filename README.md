# MiniLox — Java Programming Language Interpreter

MiniLox is a custom programming language interpreter built from scratch using Java 21. It includes a lexer, recursive-descent parser, Abstract Syntax Tree (AST), tree-walk interpreter, and a Spring Boot REST API.

The project demonstrates core programming language concepts, object-oriented design, interpreter architecture, automated testing, execution safety, and REST API development.

## Features

### Language implementation
- Lexical analysis and tokenization
- Recursive-descent parsing
- Abstract Syntax Tree (AST) and Visitor pattern
- Arithmetic, comparison, equality and logical expressions
- Variables, assignment and nested scopes
- Conditional statements (`if`, `else`)
- Loops (`while`, `for`)
- Functions, parameters and return statements
- Recursion and closures
- Native `clock()` function
- Syntax and runtime error handling

### Reliability and execution safety
- Structured diagnostics
- Collected lexer errors
- Execution step limits
- Execution timeout
- Maximum call depth
- Source code, output and string length limits
- Golden tests and deterministic fuzz testing
- JaCoCo code coverage reporting

### REST API
- Spring Boot REST API
- Execute MiniLox programs through HTTP
- JSON request and response handling
- Health-check endpoint
- Syntax and runtime error responses
- Playground execution limits

## Architecture

```text
MiniLox
│
├── minilox-core
│   └── Java Interpreter
│       ├── Lexer
│       ├── Parser
│       ├── AST
│       ├── Interpreter
│       ├── Environment
│       └── Error Handling
│
└── minilox-web
    └── Spring Boot REST API
        ├── RunController
        ├── RunService
        ├── RunRequest
        └── RunResponse
```

The web module depends on the core module. The core interpreter does not depend on Spring Boot.

## Technology Stack

- Java 21+
- Maven
- Spring Boot 4
- Spring MVC
- JUnit 5
- JaCoCo
- Git and GitHub

## Project Structure

```text
minilox/
├── minilox-core/
│   ├── src/main/java/com/minilox/
│   │   ├── ast/
│   │   ├── error/
│   │   ├── lexer/
│   │   ├── parser/
│   │   ├── interpreter/
│   │   ├── MiniLox.java
│   │   ├── MiniLoxRunner.java
│   │   └── RunResult.java
│   └── src/test/java/
│
├── minilox-web/
│   ├── src/main/java/com/minilox/web/
│   ├── src/main/resources/
│   ├── src/test/java/
│   └── pom.xml
│
├── docs/
├── examples/
├── pom.xml
└── README.md
```

## Getting Started

### Prerequisites

- JDK 21 or later
- Apache Maven 3.9+
- Git

### Clone the repository

```bash
git clone https://github.com/Deepraje-Sonone/minilox.git
cd minilox
```

### Build the project

```bash
mvn clean package
```

### Run the tests

```bash
mvn clean test
```

### Generate the coverage report

```bash
mvn -pl minilox-core test jacoco:report
```

Open:

```text
minilox-core/target/site/jacoco/index.html
```

### Run the REST API

```bash
java -jar minilox-web/target/minilox-web-0.1.0-SNAPSHOT.jar
```

The API starts on port `8080` by default.

## REST API Documentation

### 1. Health Check

**Endpoint:** `GET /api/health`

Example:

```bash
curl http://localhost:8080/api/health
```

Response:

```json
{
  "status": "ok"
}
```

### 2. Execute a MiniLox Program

**Endpoint:** `POST /api/run`

**Content-Type:** `application/json`

Request:

```json
{
  "code": "print 1 + 1;"
}
```

Successful response:

```json
{
  "ok": true,
  "output": [
    "2"
  ],
  "errors": [],
  "durationMs": 11
}
```

### Syntax Error Example

Request:

```json
{
  "code": "let = ;"
}
```

Response:

```json
{
  "ok": false,
  "output": [],
  "errors": [
    {
      "kind": "SYNTAX",
      "line": 1,
      "message": "Expect variable name."
    }
  ],
  "durationMs": 1
}
```

### Runtime Error Example

Request:

```json
{
  "code": "print unknownVariable;"
}
```

The API returns `ok: false` with a `RUNTIME` diagnostic.

### HTTP Status Codes

| Status | Meaning |
|---|---|
| 200 | Program execution completed, including syntax/runtime failures |
| 400 | Missing or blank source code |
| 500 | Unexpected server-side error |

### Execution Limits

The REST API uses `RunLimits.playground()`:

| Limit | Maximum |
|---|---:|
| Call depth | 200 |
| Execution steps | 2,000,000 |
| Execution timeout | 3 seconds |
| Output lines | 500 |
| Output characters | 20,000 |
| String length | 100,000 |
| Source characters | 10,000 |

## Testing

The project includes automated tests for:

- Lexer and tokenization
- Parser and AST construction
- Expression evaluation
- Variables and scopes
- Control flow
- Functions and closures
- Runtime and syntax errors
- Execution limits
- REST API endpoints
- Example output verification
- Randomized malformed-program robustness

Run all tests using:

```bash
mvn clean test
```

## Design Decisions

- **Recursive-descent parser:** Provides a straightforward way to implement grammar rules.
- **Visitor pattern:** Separates AST structure from operations performed on it.
- **Environment chain:** Supports nested scopes and closures.
- **Tree-walk interpreter:** Evaluates AST nodes directly.
- **RunLimits:** Protects API execution against excessive resource consumption.
- **Modular Maven architecture:** Keeps the interpreter independent of the web framework.

## Future Improvements

- Public cloud deployment
- Interactive browser-based playground
- Additional language features
- Improved API documentation and integration examples

## Author

**Deepraje Sonone**

B.Tech Computer Science and Engineering

GitHub: [Deepraje-Sonone](https://github.com/Deepraje-Sonone)

LinkedIn: [Deepraje Sonone](https://www.linkedin.com/in/deepraje-sonone-892a62244/)

---

Built as a learning project to explore programming language implementation and Java application development.
