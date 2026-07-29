package menus;

import java.util.Scanner;

import models.User;
import utils.ConsoleManager;

public class ViewUserMenu implements Menu {

    public void render() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter a username to search: ");
        String username = scanner.nextLine();

        User user = ConsoleManager.findUserByUsername(username);
        System.out.println(user);

        ConsoleManager.navigate("MainMenu");

    }
  
}
