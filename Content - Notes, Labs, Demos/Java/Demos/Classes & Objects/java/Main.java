public class Main {
    public static void main(String ...args) {

//        MyClass callItWhateverWeWAnt = new MyClass();
//        MyClass kyle = new MyClass("Kyle", "Password123");
//        MySecondClass a = new MySecondClass(1,2);
//        MySecondClass b = new MySecondClass();
//        System.out.println(a.getMyStaticValue());
//        System.out.println(b.getMyStaticValue());
//        MySecondClass actuallyTheThirdClass = new MyThirdClass(1,2);

        Calculator calc = new MyOtherCalc();
        System.out.println(calc.add(5, 10));
    }

    public void primitives(){
        boolean a = true;
        Boolean bool = true;

        char b;
        Character character = 'A';

        short c;
        Short myShort = 123;

        int d;
        Integer integer = 123456;


        long e;
        Long myLong = 2000000000l;


        float f;
        Float myFloat = 0.000000000000000000000001f;

        double g;
        Double myDouble = 0.000000000000000000000001d;

        byte h;
        Byte myByte = 'A';




    }
}
