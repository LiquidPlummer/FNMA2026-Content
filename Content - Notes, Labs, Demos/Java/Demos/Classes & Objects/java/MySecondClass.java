public class MySecondClass {
    public static int myStaticValue = 0;
    private static int objectCount = 0;
    public int myObjectValue = 0;

    public MySecondClass() {
        incrementCount();
    }

    public MySecondClass(int myObjectValue, int staticVal) {
        incrementCount();
        this.myObjectValue = myObjectValue;
        this.myStaticValue = staticVal;
    }

    public void incrementCount() {
        objectCount++;
    }

    public static int getMyStaticValue() {
        return myStaticValue;
    }

    public static void setMyStaticValue(int myStaticValue) {
        MySecondClass.myStaticValue = myStaticValue;
    }

    public int getMyObjectValue() {
        return myObjectValue;
    }

    public void setMyObjectValue(int myObjectValue) {
        this.myObjectValue = myObjectValue;
    }
}
