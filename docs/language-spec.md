
# MiniLox Language Specification

## 1. Variables
let name = "Deepraje";
let age = 22;
age = 23;

## 2. Data Types
- Number: 10, 3.14
- String: "Hello"
- Boolean: true, false
- Null: null

## 3. Arithmetic Operators
+  -  *  /  %

## 4. Comparison Operators
==  !=  <  <=  >  >=

## 5. Logical Operators
and  or  !

## 6. Output
print "Hello, MiniLox!";
print 10 + 20;

## 7. Conditions
if (age >= 18) {
    print "Adult";
} else {
    print "Minor";
}

## 8. Loops
while (age < 25) {
    age = age + 1;
}

for (let i = 0; i < 5; i = i + 1) {
    print i;
}

## 9. Functions
fn greet(name) {
    print "Hello, " + name;
}

greet("World");

## 10. Return
fn add(a, b) {
    return a + b;
}

print add(5, 3);

## 11. Comments
// This is a single-line comment

## 12. Rules
- Statements end with a semicolon.
- Blocks use curly braces.
- Variables are declared using let.
- Functions are declared using fn.
- Numbers use double internally.
- Only false and null are considered false.
