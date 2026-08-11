package services;

import daos.DepartmentDao;
import models.Department;

import java.sql.SQLException;

public class DepartmentService {
    DepartmentDao departmentDao;

    public DepartmentService(DepartmentDao departmentDao) {
        this.departmentDao = departmentDao;
    }


    public Department createDept(Department dept) {
        try {
            return this.departmentDao.saveDepartment(dept);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Department findDepartmentByName(String name) {

        try {
            return this.departmentDao.findDepartmentByName(name);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
