public class Main {

    public static void main(String[] args) {
        Driver d = new Driver(new Dependency1(), new Dependency2(new Dependency3()));
    }
}
