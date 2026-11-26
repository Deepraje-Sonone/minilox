
import { useState, useRef, useEffect } from "react";
import Editor from "@monaco-editor/react";

import {
  miniloxLanguage,
  miniloxConfiguration,
} from "./language/miniloxLanguage";

import { miniLoxExamples } from "./examples/miniLoxExamples";

import "./App.css";

const starterCode = `// Welcome to MiniLox Playground!

let name = "MiniLox";
let version = 1;

print "Hello, " + name + "!";
print "Version: " + version;

fn greet(person) {
    print "Welcome, " + person;
}

greet("Developer");
`;

function App() {
  const [code, setCode] = useState(starterCode);

  const [output, setOutput] = useState(
    "Your program output will appear here..."
  );

  const [status, setStatus] = useState("Ready");
  const [executionTime, setExecutionTime] = useState(null);
  const [isRunning, setIsRunning] = useState(false);

  const [cursorPosition, setCursorPosition] = useState({
    line: 1,
    column: 1,
  });

  const [outputLineCount, setOutputLineCount] = useState(0);

  const runRef = useRef(null);
  const resetRef = useRef(null);
  const outputRef = useRef(null);

  // Automatically scroll console when output changes.
  useEffect(() => {
    if (outputRef.current) {
      outputRef.current.scrollTop =
        outputRef.current.scrollHeight;
    }
  }, [output]);

  // Register MiniLox language.
  const handleEditorBeforeMount = (monaco) => {
    const languageExists = monaco.languages
      .getLanguages()
      .some((language) => language.id === "minilox");

    if (!languageExists) {
      monaco.languages.register({
        id: "minilox",
      });
    }

    monaco.languages.setMonarchTokensProvider(
      "minilox",
      miniloxLanguage
    );

    monaco.languages.setLanguageConfiguration(
      "minilox",
      miniloxConfiguration
    );
  };

  // Update output and line count together.
  const updateConsole = (text) => {
    setOutput(text);
    setOutputLineCount(
      text ? text.split("\n").length : 0
    );
  };

  // Execute MiniLox program.
  const handleRun = async () => {
    if (isRunning) return;

    setIsRunning(true);
    setStatus("Running");
    updateConsole("Executing your MiniLox program...");
    setExecutionTime(null);

    const startTime = performance.now();

    try {
      const response = await fetch(
        "https://minilox-api.onrender.com/api/run",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ code }),
        }
      );

      const result = await response.json();

      const elapsedTime = Math.round(
        performance.now() - startTime
      );

      setExecutionTime(
        result.durationMs ?? elapsedTime
      );

      // Handle interpreter errors.
      if (result.errors?.length > 0) {
        const errorText = result.errors
          .map((error) => {
            const lineInfo =
              error.line > 0
                ? `Line ${error.line}`
                : "General Error";

            return `${error.kind} ERROR\n${lineInfo}\n${error.message}`;
          })
          .join("\n\n--------------------\n\n");

        const errorLabels = {
          SYNTAX: "Syntax Error",
          RUNTIME: "Runtime Error",
          LIMIT: "Execution Limit",
          INTERNAL: "Internal Error",
        };

        updateConsole(
          `${errorText}\n\nExecution failed.`
        );

        setStatus(
          errorLabels[result.errors[0].kind] ??
            "Execution Error"
        );

        return;
      }

      // Handle successful execution.
      if (response.ok && result.ok) {
        const programOutput = Array.isArray(result.output)
          ? result.output.join("\n")
          : result.output ?? "";

        updateConsole(
          programOutput || "Program executed successfully."
        );

        setStatus("Success");
      } else {
        updateConsole(
          result.message ??
            "The server could not execute your program."
        );

        setStatus("Execution Error");
      }
    } catch {
      updateConsole(
        "Could not connect to the MiniLox API.\n\n" +
          "Please check your internet connection and try again."
      );

      setStatus("Connection Failed");
      setExecutionTime(null);
    } finally {
      setIsRunning(false);
    }
  };

  // Reset editor.
  const handleReset = () => {
    if (isRunning) return;

    setCode(starterCode);
    updateConsole("Your program output will appear here...");
    setStatus("Ready");
    setExecutionTime(null);
    setCursorPosition({ line: 1, column: 1 });
  };

  runRef.current = handleRun;
  resetRef.current = handleReset;

  // Clear console.
  const handleClear = () => {
    updateConsole("");
    setExecutionTime(null);
    setStatus("Ready");
  };

  // Load example.
  const handleExampleChange = (event) => {
    const selectedExample = miniLoxExamples.find(
      (example) => example.name === event.target.value
    );

    if (selectedExample) {
      setCode(selectedExample.code);
      updateConsole("Your program output will appear here...");
      setStatus("Ready");
      setExecutionTime(null);
      setCursorPosition({ line: 1, column: 1 });
    }
  };

  // Register shortcuts and cursor tracking.
  const handleEditorDidMount = (editor, monaco) => {
    editor.addAction({
      id: "minilox-run-code",
      label: "Run MiniLox Code",
      keybindings: [
        monaco.KeyMod.CtrlCmd | monaco.KeyCode.Enter,
      ],
      run: () => runRef.current?.(),
    });

    editor.addAction({
      id: "minilox-reset-editor",
      label: "Reset MiniLox Editor",
      keybindings: [
        monaco.KeyMod.CtrlCmd |
          monaco.KeyMod.Shift |
          monaco.KeyCode.KeyR,
      ],
      run: () => resetRef.current?.(),
    });

    editor.onDidChangeCursorPosition((event) => {
      setCursorPosition({
        line: event.position.lineNumber,
        column: event.position.column,
      });
    });
  };

  const statusClass = status
    .toLowerCase()
    .replace(/[^a-z]+/g, "-")
    .replace(/^-|-$/g, "");

  const isError = [
    "Syntax Error",
    "Runtime Error",
    "Execution Limit",
    "Internal Error",
    "Execution Error",
    "Connection Failed",
  ].includes(status);

  return (
    <div className="app">
      <header className="topbar">
        <div className="brand">
          <div className="brand-icon">{"</>"}</div>

          <div>
            <h1>MiniLox</h1>
            <span>Programming Language Playground</span>
          </div>
        </div>

        <div className="header-right">
          <span className="language-label">
            Language: MiniLox
          </span>
          <span className="version-label">v1.0</span>
        </div>
      </header>

      <main className="workspace">
        <section className="editor-section">
          <div className="panel-header">
            <div className="panel-title">
              <span className="file-icon">{"</>"}</span>
              <span>main.mlox</span>
            </div>

            <div className="editor-actions">
              <select
                className="example-select"
                defaultValue=""
                onChange={handleExampleChange}
                disabled={isRunning}
              >
                <option value="" disabled>
                  Select Example
                </option>

                {miniLoxExamples.map((example) => (
                  <option
                    key={example.name}
                    value={example.name}
                  >
                    {example.name}
                  </option>
                ))}
              </select>

              <button
                className="reset-button"
                onClick={handleReset}
                disabled={isRunning}
                title="Reset editor (Ctrl + Shift + R)"
              >
                Reset
              </button>

              <button
                className="run-button"
                onClick={handleRun}
                disabled={isRunning}
                title="Run code (Ctrl + Enter)"
              >
                {isRunning ? "Running..." : "▶ Run Code"}
              </button>
            </div>
          </div>

          <div className="editor-container">
            <Editor
              height="100%"
              language="minilox"
              theme="vs-dark"
              value={code}
              beforeMount={handleEditorBeforeMount}
              onMount={handleEditorDidMount}
              onChange={(value) => setCode(value ?? "")}
              options={{
                fontSize: 14,
                minimap: { enabled: true },
                scrollBeyondLastLine: false,
                automaticLayout: true,
                wordWrap: "on",
                tabSize: 4,
                insertSpaces: true,
                autoIndent: "full",
                formatOnPaste: true,
                formatOnType: true,
                bracketPairColorization: { enabled: true },
                guides: {
                  bracketPairs: true,
                  indentation: true,
                  highlightActiveIndentation: true,
                },
                folding: true,
                foldingHighlight: true,
                showFoldingControls: "always",
                find: { addExtraSpaceOnTop: false },
                padding: { top: 15 },
                lineNumbers: "on",
                renderLineHighlight: "all",
                cursorBlinking: "smooth",
                smoothScrolling: true,
                matchBrackets: "always",
                readOnly: isRunning,
              }}
            />
          </div>

          <div className="editor-footer">
            <span>MiniLox</span>
            <span>
              Ln {cursorPosition.line}, Col {cursorPosition.column}
              {" | "}
              Ctrl + Enter to Run
            </span>
          </div>
        </section>

        <section className="output-section">
          <div className="output-header">
            <div className="panel-title">
              <span className="terminal-icon">{"⌘"}</span>
              <span>Console Output</span>
            </div>

            <button
              className="clear-button"
              onClick={handleClear}
              disabled={isRunning}
            >
              Clear
            </button>
          </div>

          <div className="output-content" ref={outputRef}>
            <div className="output-status">
              <span className={`status-dot ${statusClass}`} />
              <span>{status}</span>
            </div>

            {output ? (
              <pre className={isError ? "error-output" : "success-output"}>
                {output}
              </pre>
            ) : (
              <div className="console-empty">
                <span className="empty-icon">{"⌘"}</span>
                <p>Console is empty</p>
                <span>Run your MiniLox program to see its output.</span>
              </div>
            )}

            {executionTime !== null && (
              <div className="execution-time">
                Execution time: {executionTime} ms
              </div>
            )}
          </div>

          <div className="output-footer">
            <span>MiniLox Console</span>
            <span>
              {outputLineCount} output lines
            </span>
          </div>
        </section>
      </main>

      <footer className="app-footer">
        <span>
          MiniLox — Built with Java and Spring Boot
        </span>
        <span>React + Monaco Editor</span>
      </footer>
    </div>
  );
}

export default App;
