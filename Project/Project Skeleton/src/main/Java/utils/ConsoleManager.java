package utils;

import java.util.HashMap;
import java.util.Map;

import menus.AnotherMenu;
import menus.MainMenu;
import menus.Menu;
import menus.OtherMenu;

/*
 * This console manager exists to allow STD IN/OUT interaction during development. 
 * This will be unnecessary once we put an API in the presentation layer. For now
 * we can consider this to be our presentation layer.
 */
public class ConsoleManager {
    public static boolean running = true;
    public static Map<String, Menu> menuMap;
    public static Menu nextMenu;

    public static void init() {
        running = true;
        
        menuMap = new HashMap<>();
        menuMap.put("MainMenu", new MainMenu());
        menuMap.put("OtherMenu", new OtherMenu());
        menuMap.put("AnotherMenu", new AnotherMenu());

        //Here's the main application loop. It will keep rendering the next menu until 'running' becomes false.
        navigate("MainMenu");
        while(running) {
            nextMenu.render();
        }
        System.out.println("Quitting... Bye!");
    }

    public static void navigate(String menuName) {
        nextMenu = menuMap.get(menuName);
    }

    public static void quit() {
        running = false;
    }

}
