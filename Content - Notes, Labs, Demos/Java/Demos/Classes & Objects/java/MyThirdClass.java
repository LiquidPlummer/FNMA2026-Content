public class MyThirdClass extends MySecondClass{
    public MyThirdClass(int a, int b) {
        super(a, b);
        System.out.println("A ThirdClass was instantiated.");
    }

    public void whatever()/*This is the "abstraction" AKA the signature AKA interface*/ {
        //this is the "concretion" AKA "implementation" AKA "body"
    };
}
