package daos;

import models.User;
import utils.ConnectionManager;

import java.sql.*;

public class UserDao {
    User fakeUserStore;

    public void fakeDaoMethod() {
        return;
    }


    /*
    This fake functionality is just here until we replace it with real database JDBC
     */
    public User create(User user) throws SQLException {
        /*
        We need to disassemble the user object into it's various variables, and
        build a SQL script that will INSERT those valuse.
        We do this with Statements, PreparedStatements, and by parameterizing.
         */
        //PATTERN A
        Connection conn = ConnectionManager.getConnection();

        String sql = "INSERT INTO users (username, password, first_name, last_name, dept, \"role\") VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);//this flag gets us the keys later
        pstmt.setString(1, user.getUsername());
        pstmt.setString(2, user.getPassword());
        pstmt.setString(3, user.getFirstName());
        pstmt.setString(4, user.getLastName());
        pstmt.setString(5, user.getDept());
        pstmt.setString(6, user.getRole());

        pstmt.executeUpdate();

        //END OF PATTERN A

        //This is how we get the keys back after inserting. Will be important later.
        ResultSet rs = pstmt.getGeneratedKeys();
        if(rs.next()){
            user.setId(rs.getInt(1));
        }

        conn.close();
        return user;
    }

    public User findUserByUsername(String username) throws SQLException {
        //PATTERN A

        //get connection
        Connection conn = ConnectionManager.getConnection();

        //write the SQL string
        String sql = "SELECT * FROM users WHERE username = ?";

        //prepare the statement
        PreparedStatement pstmt = conn.prepareStatement(sql);

        //parameterize the statement
        pstmt.setString(1, username);

        //execute the statement
        ResultSet rs = pstmt.executeQuery();

        //END OF PATTERN A, START PATTERN B

        //Iterate through the ResultSet
        //What do we do with the results? We "marshall" them
        User resultUser = new User();
        if(rs.next()) {
            resultUser.setId(rs.getInt("id"));
            resultUser.setUsername(rs.getString("username"));
            resultUser.setPassword(rs.getString("password"));
            resultUser.setFirstName(rs.getString("first_name"));
            resultUser.setLastName(rs.getString("last_name"));
            resultUser.setDept(rs.getString("dept"));
            resultUser.setRole(rs.getString("role"));
        }

        //END OF PATTERN B

        return resultUser;
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
