import controllers.DepartmentController;
import controllers.ReimbursementController;
import controllers.UserController;
import daos.DepartmentDao;
import daos.ReimbursementDao;
import daos.UserDao;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.json.JavalinJackson3;
import models.Reimbursement;
import models.User;
import services.DepartmentService;
import services.ReimbursementService;
import services.UserService;
import utils.ConsoleManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
//        ConsoleManager.init();

        Javalin.create(Main::configureJavalinServer).start(7000);
    }


    /**
     * Configures the Javalin server instance: wires up the application's dependency graph,
     * registers the JSON mapper, and maps HTTP endpoints to their handler methods.
     * <p>
     * Controllers are constructed here using constructor-based dependency injection, so each
     * controller receives its service, and each service receives its DAO. Jackson 3 is registered
     * as the JSON mapper for request/response serialization.
     * <p>
     * Registered routes:
     * <ul>
     *   <li>{@code POST /departments} &rarr; {@link DepartmentController#postDept}</li>
     * </ul>
     *
     * @param config the {@link JavalinConfig} supplied by Javalin during server startup,
     *               used to register the JSON mapper and route definitions
     */
    public static void configureJavalinServer(JavalinConfig config) {
        //DEPENDENCY INJECTION programming pattern
        DepartmentController departmentController = new DepartmentController(new DepartmentService(new DepartmentDao()));
        UserService userService = new UserService(new UserDao());
        UserController userController = new UserController(userService);
        ReimbursementDao reimbursementDao = new ReimbursementDao();
        ReimbursementController reimbursementController = new ReimbursementController(new ReimbursementService(reimbursementDao, userService));

        //Tell Javalin to use the newer jackson tools:
        config.jsonMapper(new JavalinJackson3());

        //Map the endpoints
        //we need to specify what behavior handles what requests
        //requests are denoted by method and URL
        //So we tell javalin "This method, this endpoint, maps to that behavior"
        config.routes.post("/departments", departmentController::postDept);
        config.routes.get("/departments", departmentController::getDepartments);
        config.routes.get("/departments/{name}", departmentController::getDepartmentByName);
        config.routes.put("/departments/{name}", departmentController::updateDepartment);
        config.routes.delete("/departments/{name}", departmentController::deleteDepartment);

        config.routes.post("/reimbursements", reimbursementController::postNewReimbursement);
        config.routes.get("/reimbursements", reimbursementController::getReimbursementWithFilters);
        config.routes.get("/reimbursements/{id}", reimbursementController::getReimbursementById);
        config.routes.put("/reimbursements/{id}", reimbursementController::updateReimbursement);
        config.routes.delete("/reimbursements/{id}", reimbursementController::deleteReimbursement);



        //TESTING SOMETHING - TODO: GET RID OF THIS!
        Map<String, String> map = new HashMap<>();
        map.put("author_id", "1");
        map.put("amount", "123.45");
        map.put("type", "test");
        List<Reimbursement> list = reimbursementDao.getReimbursementsWithFiltering(map);
        System.out.println("RESULTS: ");
        for(Reimbursement r : list) {
            System.out.println(r);
        }




    }
}
