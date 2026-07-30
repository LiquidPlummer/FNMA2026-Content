package daos;

import models.Department;
import utils.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DepartmentDao {
    public void fakeDaoMethod() {
        return;
    }

    /*
    CREATE
    READ ONE - by PK
    UPDATE
    DELETE
     */

    public Department saveDepartment(Department dept) throws SQLException {
        //PATTERN A
        //get the connection
        Connection conn = ConnectionManager.getConnection();
        //write the SQL
        String sql = "INSERT INTO departments (name) VALUES (?)";
        //prepare the statement
        PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
        //parameterize the statement
        pstmt.setString(1, dept.getName());
        //execute the statement
        pstmt.executeUpdate();
        //PATTERN B
        ResultSet rs = pstmt.getGeneratedKeys();
        //marshall the results
        if(rs.next()) {
            dept.setId(rs.getInt(1));
        }
        return dept;
    }
}
