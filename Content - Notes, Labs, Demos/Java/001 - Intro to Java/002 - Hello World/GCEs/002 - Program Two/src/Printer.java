/*
 * Printer is a utility class we've written to handle printing.
 * Separating this responsibility into its own class is an example of
 * organizing code by concern — a principle we'll explore more later.
 */
public class Printer {

    /*
     * printString() is a static method that takes a String and prints it.
     * 'static' means this method belongs to the class itself, but let's 
     * not worry about that detail for now. 
     * 'String s' is the parameter — the text passed in to be printed.
     */
    public static void printString(String s) {

        /*
         * System.out.println() prints the given string to the console.
         * Here we're passing along whatever string was handed to this method.
         */
        System.out.println(s);
    }
}