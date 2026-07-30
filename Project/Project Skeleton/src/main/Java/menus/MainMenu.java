package menus;

import java.util.Scanner;

import utils.ConsoleManager;

public class MainMenu implements Menu{
    public String name = "MainMenu";

    /**
     * The render() method is just a series of outputs and inputs. Output some prompt for the user, take in their input
     * and act on it. The render method is basically a UI script for the console. At the end of each render, the console
     * manager will check if the app should continue running, then call the next render() method. Queue up the next menu with 
     * navigate()
     */
    public void render() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("==================== Welcome! ====================\n"
            + "This is how we create menus. Just output instructions for the user\n"
            + "and then read in their choice with scanner.\n"
            + "1) Get User by Username\n"
            + "2) Create New User\n"
            + "3) Create New Department\n"
            + "Q) Quit\n"
            );
        
        String input = scanner.nextLine();

        switch(input) {
            case "1":
                ConsoleManager.navigate("ViewUserMenu");
                break;
            case "2": 
                ConsoleManager.navigate("NewUserMenu");
                break;
            case "3":
                ConsoleManager.navigate("NewDeptMenu");
                break;
            case "4":
                ConsoleManager.navigate("ViewUsersForDeptMenu");
                break;
            case "Q":
            case "q":
                ConsoleManager.quit();
                break;
        }
    }
}
