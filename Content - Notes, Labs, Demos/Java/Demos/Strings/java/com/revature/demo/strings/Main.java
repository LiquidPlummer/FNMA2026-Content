package com.revature.demo.strings;

public class Main {
    public static void main(String[] args) {
        //We have 8 primitives, and otherwise everything is a reference.
        //we have the 8 wrapper classes

        //Strings also have a bunch of "syntactic sugar"
        //A string is really an array of characters.

        //STRINGS ARE IMMUTABLE!

        String hello = "Hello World";//0x12345
        String anotherString = "Hello World";//0x12345
        String myString = new String("Hello World");//0xFFEEEEEEEEEEF
        myString = myString.intern();//0x12345

        if(hello == anotherString) {
            System.out.println("They are equal");
        } else {
            System.out.println("NOT EQUAL!!!!");
        }

        if(hello == myString) {
            System.out.println("Also these are equal");
        } else {
            System.out.println("EQUIVALENT NOT EQUAL");
        }

        Object object = new Object();//0x123455
        Object obj2 = new Object();//0xFFEEF

        System.out.println(object == obj2);
        System.out.println(object.equals(obj2));//should be true...?


        String greeting = "Hello";
        greeting += ", folks!";

        for(int i = 0; i < 100000; i++) {
            greeting += "!";
        }


        StringBuilder sb = new StringBuilder(greeting);
        for(int i = 0; i < 100000; i++) {
            sb.append("!");
        }
        greeting = sb.toString();



        System.out.println(greeting);



    }
}
