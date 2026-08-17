package com.revature.SPR_GCE_ERRORS;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;

@Service
public class ErrorService {
    public void throwException() throws Exception {
        switch((int)Math.ceil(Math.random() * 4)) {
            case 1:
                throw new SQLException("Sql Exception, we should never see this text in the response.");
            case 2:
                throw new IOException("IO Exception");
            case 3:
                throw new ClassNotFoundException("Class Not Found Exception");
            case 4:
                throw new InterruptedException("Interrupted Exception");

        }
    }
}
