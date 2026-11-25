
package com.minilox;

public class MiniLox {

    public static void main(String[] args) {

        System.out.println("MiniLox interpreter v0.1");
        System.out.println("Arguments received: " + args.length);

        int number = 1;

        for (String arg : args) {
            System.out.println("Argument " + number + ": " + arg);
            number++;
        }
    }
}
