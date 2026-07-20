/*
 * HelloWorld is a class. In Java, all code lives inside a class.
 * The file name must match the class name (HelloWorld.java).
 */
public class HelloWorld {

    /*
     * main() is the entry point of a Java program.
     * When you run this program, the JVM looks for and calls this method.
     * 'public' and 'static' are access/behavior modifiers — topics we'll explore later.
     * 'void' means this method returns nothing.
     * 'String[] args' holds any command-line arguments passed at runtime.
     */
    public static void main(String[] args) {

        /*
         * System.out.println() prints a line of text to the console.
         * 'System' is a built-in Java class. 'out' is its output stream.
         * 'println' prints the given string followed by a newline.
         */
        System.out.println("Hello, World!");
    }
}