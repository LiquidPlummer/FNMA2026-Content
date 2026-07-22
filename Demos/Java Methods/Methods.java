/** 
 * Everything in Java is a class.
 * Functions and Variables in java: Methods and Fields.
 *
 * 
 */


public class Methods{
    //Inside the class we are in class scope.
    static int classScopedVariable = 6;//class scoped x

    public static void main(String[] args) {
        int methodScopeVariable = 10;

        System.out.println(args[0]);
        main(args);


        for(int i = 0; i < 10; i++) {
            System.out.println(classScopedVariable);
            System.out.println(methodScopeVariable);
            int blockScopedVariable = 11;
        }

        // System.out.println(blockScopedVariable);
    }


    {
        String[] weCanCallThiWhateverWeWant = new String[]{"Hello", "World"};
        
        System.out.println(classScopedVariable);
        // System.out.println(methodScopeVariable);
        
        newMethod();
        
        
        main(weCanCallThiWhateverWeWant);
    }



    private void newMethod(/*0 or more params */) {

    }





    
}