package menus;

import java.util.Scanner;

import utils.ConsoleManager;

public class AnotherMenu implements Menu {

    public void render() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("==================== Another Menu ====================\n"
            + "This is 'Another Menu'.\n"
            + "1) Main Menu\n"
            + "2) Other Menu\n"
            + "Q) Quit\n"
            );
        
        String input = scanner.nextLine();

        switch(input) {
            case "1":
                ConsoleManager.navigate("MainMenu");
                break;
            case "2": 
                ConsoleManager.navigate("OtherMenu");
                break;
            case "Q":
            case "q":
                ConsoleManager.quit();
                break;
        }
    }
  
}
