
export const miniLoxExamples = [
  {
    name: "Hello World",
    code: `// Hello World in MiniLox

print "Hello, MiniLox!";
`,
  },
  {
    name: "Variables and Arithmetic",
    code: `// Variables and arithmetic

let a = 10;
let b = 20;

let sum = a + b;
let difference = a - b;
let product = a * b;

print "Sum:";
print sum;

print "Difference:";
print difference;

print "Product:";
print product;
`,
  },
  {
    name: "If-Else Conditions",
    code: `// If-Else example

let age = 20;

if (age >= 18) {
    print "You are an adult.";
} else {
    print "You are a minor.";
}
`,
  },
  {
    name: "While Loop",
    code: `// While loop example

let count = 1;

while (count <= 5) {
    print count;
    count = count + 1;
}
`,
  },
  {
    name: "Functions",
    code: `// Function example

fn greet(name) {
    print "Hello, " + name;
}

greet("Developer");
greet("MiniLox");
`,
  },
  {
    name: "Recursion",
    code: `// Recursive factorial function

fn factorial(n) {
    if (n <= 1) {
        return 1;
    }

    return n * factorial(n - 1);
}

print factorial(5);
`,
  },
];
