import java.util.Scanner;

public class Main {
    static String input = "FACE".toLowerCase();


    public static void main(String[] args) {
        String result = "";


        for(char c : input.toCharArray()){
            if(c > 106){
                result += '?';
                continue;
            }
            result += (char)(c - 48);
        }


        System.out.println(result);


    }
}
