package menus;

import java.util.Scanner;

import utils.ConsoleManager;

public class OtherMenu implements Menu {
    /**
     * The render() method is just a series of outputs and inputs. Output some prompt for the user, take in their input
     * and act on it. The render method is basically a UI script for the console. At the end of each render, the console
     * manager will check if the app should continue running, then call the next render() method. Queue up the next menu with 
     * navigate()
     */
    public void render() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("==================== Other Menu ====================\n"
            + "This is the 'Other Menu'.\n"
            + "1) Main Menu\n"
            + "2) Another Menu\n"
            + "Q) Quit\n"
            );
        
        String input = scanner.nextLine();

        switch(input) {
            case "1":
                ConsoleManager.navigate("MainMenu");
                break;
            case "2": 
                ConsoleManager.navigate("AnotherMenu");
                break;
            case "Q":
            case "q":
                ConsoleManager.quit();
                break;
        }
    }

}
