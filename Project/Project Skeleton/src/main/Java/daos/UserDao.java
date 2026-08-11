package daos;

import models.User;
import utils.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

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

        String sql = "INSERT INTO users (username, password, first_name, last_name, dept_id, \"role\") VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);//this flag gets us the keys later
        pstmt.setString(1, user.getUsername());
        pstmt.setString(2, user.getPassword());
        pstmt.setString(3, user.getFirstName());
        pstmt.setString(4, user.getLastName());
        pstmt.setInt(5, user.getDept_id());
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
            resultUser.setDept_id(rs.getInt("dept_id"));
            resultUser.setRole(rs.getString("role"));
        }

        //END OF PATTERN B

        return resultUser;
    }

    public User findUserById(Integer id) throws SQLException {
        Connection conn = ConnectionManager.getConnection();
        String sql = "SELECT * FROM users WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, id);
        ResultSet rs = pstmt.executeQuery();
        User resultUser = new User();
        if(rs.next()) {
            resultUser.setId(rs.getInt("id"));
            resultUser.setUsername(rs.getString("username"));
            resultUser.setPassword(rs.getString("password"));
            resultUser.setFirstName(rs.getString("first_name"));
            resultUser.setLastName(rs.getString("last_name"));
            resultUser.setDept_id(rs.getInt("dept_id"));
            resultUser.setRole(rs.getString("role"));
        }

        return resultUser;
    }

    public List<User> getUsersByDepartment(String dept) throws SQLException {
        //PATTERN A+B
        Connection conn = ConnectionManager.getConnection();
        String sql = "SELECT id, username, first_name, last_name, U.dept_id, ROLE, name AS dept_name FROM users U JOIN departments D ON D.dept_id = U.dept_id WHERE D.name = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, dept);
        ResultSet rs = pstmt.executeQuery();

        List<User> userList = new ArrayList<>();
        while(rs.next()) {
            userList.add(new User(rs.getInt("id"),rs.getString("username"), rs.getString("first_name"), rs.getString("last_name"), rs.getString("role")));
        }
        return userList;
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
