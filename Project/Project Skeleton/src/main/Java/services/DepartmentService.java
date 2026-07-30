package services;

import daos.DepartmentDao;
import models.Department;

import java.sql.SQLException;

public class DepartmentService {
    DepartmentDao departmentDao;

    public DepartmentService(DepartmentDao departmentDao) {
        this.departmentDao = departmentDao;
    }

    public void fakeServiceMethod() {
        departmentDao.fakeDaoMethod();
    }

    public Department createDept(Department dept) {
        try {
            return this.departmentDao.saveDepartment(dept);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
