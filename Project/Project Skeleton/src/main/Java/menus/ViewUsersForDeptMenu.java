package menus;

import utils.ConsoleManager;

import java.util.Scanner;

public class ViewUsersForDeptMenu implements Menu{
    @Override
    public void render() {
        Scanner sc = new Scanner(System.in);
        System.out.println("=================== View Users for Department ===================");
        System.out.print("Enter department name: ");
        String deptName = sc.nextLine();

        ConsoleManager.findUsersByDepartment(deptName);
        ConsoleManager.navigate("MainMenu");

    }
}
