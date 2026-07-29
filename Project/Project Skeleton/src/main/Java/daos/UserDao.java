package daos;

import models.User;

public class UserDao {
    User fakeUserStore;

    public void fakeDaoMethod() {
        return;
    }


    /*
    This fake functionality is just here until we replace it with real database JDBC
     */
    public void create(User user) {
        //we don't have the JDBC code to actually persist this data.
        fakeUserStore = user;
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
