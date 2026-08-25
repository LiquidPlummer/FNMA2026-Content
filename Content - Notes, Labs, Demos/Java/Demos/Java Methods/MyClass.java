class MyClass {
    public static void main(String[] args) throws Exception{
        Methods methods = new Methods();
        // methods.newMethod();

        ExampleModel model = new ExampleModel();
        String name = "kplummer";
        model.setUsername(name);


        doSomethingToModel(model);

        int i = 2;
        i = doubleNum(i);
    }


    public void doSomethingToModel(ExampleModel model) throws Exception{
        model.setUsername("changedUsername");
    }

    public int doubleNum(int i) {
        return i*2;
    }
}
