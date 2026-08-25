public class Main {
    public static void main(String[] args) {
        SimpleList<String> mySimpleList = new SimpleLinkedList<>();
        mySimpleList.add("Kyle");
        mySimpleList.add("Brayden");
        mySimpleList.add("Kirill");
        mySimpleList.add("Fathi");
        mySimpleList.add("Travis");

        for(int i = 0; i < mySimpleList.length(); i++) {
            System.out.println(mySimpleList.get(i));
        }
        System.out.println("=================================");

        for(String s : mySimpleList) {
            System.out.println(s);
        }





    }
}
