package com.revature.demos.javalin.controllers;


import com.revature.demos.javalin.models.User;
import io.javalin.http.Context;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

public class DemoController {


    public void demoHeaders(Context ctx) {
        String sentence = ctx.header("sentence");
        Integer number = Integer.parseInt(ctx.header("number"));

        Map<String, String> headers =  ctx.headerMap();

        Set<String> keys = headers.keySet();
        for(String k : keys) {
            ctx.header(k, headers.get(k));
            System.out.print("[" + k + "]: ");
            System.out.println(headers.get(k));
        }

//        ctx.header("Content-Type", "not great");
//        ctx.header("Content-Length", "0");//these ones are derived for us based on the contents of the response
        ctx.result("Result?");

    }

    public void demoBody(Context ctx) throws IOException {
        //get the body from the request:
        User bodyObj = ctx.bodyAsClass(User.class);//This one tells Jackson what class to build from the body JSON
        String bodyText = ctx.body();

        //put body in the response
        ctx.json(new User("1", "2", "3", "4"));
        ctx.result("Write string to body, this will overwrite what was written above.");

        ObjectMapper mapper = new ObjectMapper();
        String jsonRep = mapper.writeValueAsString(new User("5", "6", "7", "8"));
        ctx.result(jsonRep);



    }
}
