package utils;

import java.util.HashMap;
import java.util.Map;

import daos.DepartmentDao;
import daos.UserDao;
import menus.*;
import models.Department;
import models.User;
import services.DepartmentService;
import services.UserService;

/*
 * This console manager exists to allow STD IN/OUT interaction during development. 
 * This will be unnecessary once we put an API in the presentation layer. For now
 * we can consider this to be our presentation layer.
 */
public class ConsoleManager {
    public static boolean running = true;
    public static Map<String, Menu> menuMap;
    public static Menu nextMenu;
    public static UserService userService;
    public static DepartmentService deptService;

    public static void init() {
        running = true;

        //Later this might take a few more bits, but this is good for now.
        //This is the user vertical slice, missing the PL stuff, that comes later.
        userService = new UserService(new UserDao());
        deptService = new DepartmentService(new DepartmentDao());

        
        menuMap = new HashMap<>();
        menuMap.put("MainMenu", new MainMenu());
        menuMap.put("NewUserMenu", new NewUserMenu());
        menuMap.put("ViewUserMenu", new ViewUserMenu());
        menuMap.put("NewDeptMenu", new NewDeptMenu());

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

    public static void saveNewUSer(User newUser) {
        System.out.println("User created: " + userService.saveUser(newUser));
    }

    public static User findUserByUsername(String username) {
        return userService.findUserByUsername(username);
    }

    public static void saveNewDepartment(Department dept) {
        System.out.println(deptService.createDept(dept));
    }

}
