
export const miniloxLanguage = {
  defaultToken: "",
  tokenPostfix: ".mlox",

  keywords: [
    "let",
    "fn",
    "print",
    "if",
    "else",
    "while",
    "for",
    "return",
    "true",
    "false",
    "null",
    "and",
    "or",
  ],

  operators: [
    "=", ">", "<", "!", "==", "!=", "<=",
    ">=", "+", "-", "*", "/", "%",
  ],

  symbols: /[=><!~?:&|+\-*\/\^%]+/,

  tokenizer: {
    root: [
      // Identifiers and keywords
      [
        /[a-zA-Z_][a-zA-Z0-9_]*/,
        {
          cases: {
            "@keywords": "keyword",
            "@default": "identifier",
          },
        },
      ],

      // Numbers
      [/\d+(\.\d+)?/, "number"],

      // Strings
      [/"([^"\\]|\\.)*$/, "string.invalid"],
      [/"/, { token: "string.quote", bracket: "@open", next: "@string" }],

      // Comments
      [/\/\/.*$/, "comment"],

      // Operators
      [/@symbols/, "operator"],

      // Brackets
      [/[{}()\[\]]/, "@brackets"],

      // Separators
      [/[;,.]/, "delimiter"],
    ],

    string: [
      [/[^\\"]+/, "string"],
      [/\\./, "string.escape"],
      [/"/, { token: "string.quote", bracket: "@close", next: "@pop" }],
    ],
  },
};

export const miniloxConfiguration = {
  comments: {
    lineComment: "//",
  },

  brackets: [
    ["{", "}"],
    ["(", ")"],
    ["[", "]"],
  ],

  autoClosingPairs: [
    { open: "{", close: "}" },
    { open: "(", close: ")" },
    { open: "[", close: "]" },
    { open: '"', close: '"' },
  ],

  surroundingPairs: [
    { open: "{", close: "}" },
    { open: "(", close: ")" },
    { open: "[", close: "]" },
    { open: '"', close: '"' },
  ],
};
