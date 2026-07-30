package daos;

import models.User;
import utils.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserDao {
    User fakeUserStore;

    public void fakeDaoMethod() {
        return;
    }


    /*
    This fake functionality is just here until we replace it with real database JDBC
     */
    public void create(User user) throws SQLException {
        /*
        We need to disassemble the user object into it's various variables, and
        build a SQL script that will INSERT those valuse.
        We do this with Statements, PreparedStatements, and by parameterizing.
         */
        Connection conn = ConnectionManager.getConnection();

        String sql = "INSERT INTO users (username, password, first_name, last_name, dept, \"role\") VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement stmt = conn.prepareStatement(sql);

    }

    public User findUserByUsername(String username) {
        if(fakeUserStore.getUsername().equals(username)) {
            return fakeUserStore;
        }
        return null;
    }
}



/* What goes in DAOs? CRUD!
 * CRUD
 * CREATE(Model model)
 * READ ONE
 * READ MANY (w/ Filtering)
 * UPDATE (implemented to handle any and all fields)
 * DELETE
 */
