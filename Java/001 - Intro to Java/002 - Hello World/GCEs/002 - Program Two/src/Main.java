public class Main {

    /*
     * main() is the entry point of a Java program.
     * When you run this program, the JVM looks for and calls this method.
     * 'String[] args' holds any command-line arguments passed at runtime —
     * we'll be working with those arguments below.
     */
    public static void main(String[] args) {

        /*
         * Printer is another class we've written. printString() is a method on that class.
         * Calling methods on other classes is a core part of object-oriented programming,
         * a topic we'll explore in depth later.
         * Here we're printing a label before listing the arguments.
         */
        Printer.printString("Command line args: ");

        /*
         * A for loop repeats a block of code a set number of times.
         * 'int i = 0' initializes a counter variable starting at 0.
         * 'i < args.length' is the condition — the loop runs as long as i is less than
         * the number of arguments. 'args.length' is the count of items in the args array.
         * 'i++' increments the counter by 1 after each iteration.
         * So this loop will start at 0, the beginnig of the array, and continue to the end.
         */
        for (int i = 0; i < args.length; i++) {

            /*
             * On each iteration, we print the index and the corresponding argument.
             * 'args[i]' accesses the element at position i in the args array.
             * The '+' operator here concatenates strings together.
             */
            Printer.printString(i + ": " + args[i]);
        }  
    }
}