@FunctionalInterface
public interface MyFunctionalInterface {
    //When it comes to a "functional interface" what were really doing is trying to establish a way
    //to have just a funciton, not a whole class object. We want to pass a function as a parameter, like
    // more modern languages do (JS, Py)

    //functional interface is just a regular interface with special rules:
    //there can be only one and must be exactly one abstract method

    //default methods don't count - so in functional interfaces we need 1 abstract method, but can have
    //as many defaults as we want.

    int doubleIt(int n);
}
