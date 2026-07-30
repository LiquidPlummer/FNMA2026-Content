package menus;

import models.Department;
import utils.ConsoleManager;

import java.util.Scanner;

public class NewDeptMenu implements Menu{
    @Override
    public void render() {
        Scanner sc = new Scanner(System.in);
        System.out.println("======================== New Dept Menu ========================");
        System.out.print("Enter a new department name: ");
        String input = sc.nextLine();

        Department dept = new Department(input);
        ConsoleManager.saveNewDepartment(dept);

        ConsoleManager.navigate("MainMenu");
    }
}
