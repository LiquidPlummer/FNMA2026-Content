package services;

import daos.UserDao;
import models.User;

import java.sql.SQLException;
import java.util.List;

public class UserService {
    UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User saveUser(User newUser) {
        try {
            return this.userDao.create(newUser);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User findUserByUsername(String username) {
        try {
            return this.userDao.findUserByUsername(username);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User findUserById(Integer id) {
        try {
            return this.userDao.findUserById(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public List<User> findUsersByDepartment(String department) {
        try {
            return this.userDao.getUsersByDepartment(department);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
/*
 * Here in services is where "everything else" happens, we call this whole "Business Logic"
 * For now the service layer will be very empty, because we don't have a lot of extra business logic
 * This will fill up later as we add things like logging, validation, security. As we pass through the SL to get to the DL from PL,
 * we can fit in all sorts of operations here. 
 * 
 * So for now it will feel pointless, like we're wasting time bouncing through it for no reason.
 * This is a losely coupled place for us to add in business logic right in the middle later.
 * 
 */