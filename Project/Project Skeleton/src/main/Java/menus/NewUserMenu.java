package menus;

import java.util.Scanner;

import models.User;
import utils.ConsoleManager;

public class NewUserMenu implements Menu {
    /**
     * The render() method is just a series of outputs and inputs. Output some prompt for the user, take in their input
     * and act on it. The render method is basically a UI script for the console. At the end of each render, the console
     * manager will check if the app should continue running, then call the next render() method. Queue up the next menu with 
     * navigate()
     */
    public void render() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("==================== CREATE NEW USER ====================\n");

        System.out.print("Enter a username: ");
        String username = scanner.nextLine();

        System.out.print("Enter a password: ");
        String password = scanner.nextLine();

        System.out.print("Enter a first name: ");
        String firstName = scanner.nextLine();

        System.out.print("Enter a last name: ");
        String lastName = scanner.nextLine();

        System.out.print("Enter a department: ");
        String department = scanner.nextLine();

        System.out.print("Enter a role: ");
        String role = scanner.nextLine();


        /*
        When we get rid of this console junk we will still be able to call this function in the same way.
        This is where we go from the temp UI to the real workflow.
         */
        User newUser = new User(username, password, firstName, lastName, department, role);
        ConsoleManager.saveNewUSer(newUser);

        System.out.println("New user created successfully! (I hope...)");
        ConsoleManager.navigate("MainMenu");

    }

}
