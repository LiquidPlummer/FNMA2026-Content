public class Main {
    public static void main(String[] args) {
        MyFunctionalInterface myInterface = (int i) -> i*2;
        MyFunctionalInterface myInterface2 = Main::doesntMatterWhatYouCallMe;

        Main.testFunction(Main::doesntMatterWhatYouCallMe);



    }

    public static int doesntMatterWhatYouCallMe(int i) {
        return i*2;
    }

    public static void testFunction(MyFunctionalInterface callback){
        //now it is later!
        System.out.println(callback.doubleIt(2));
    }
}