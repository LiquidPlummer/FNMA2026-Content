public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, World!");

        System.out.println(reverse("Hello, World!"));
    }

    public static String reverse(String s) {
        StringBuilder reversed = new StringBuilder();
        for (int i = s.length() - 1; i >= 0; i--) {
            reversed.append(s.charAt(i));
        }
        return reversed.toString();
    }
}
