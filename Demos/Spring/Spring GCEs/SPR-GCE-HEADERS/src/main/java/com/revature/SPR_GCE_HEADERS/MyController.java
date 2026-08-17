package com.revature.SPR_GCE_HEADERS;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Enumeration;

@RestController
public class MyController {
    /**
     * We can use the @RequestHeader annotation to tell Spring to grab a particular header from the request and pass it
     * into this method. Here Spring will see the parameter list and the annotation, will get the value of the
     * Content-Type header, and will assign that value to the contentType parameter.
     */
    @GetMapping("/requestHeaderAnnotation")
    public String readHeaderWithAnnotation(@RequestHeader("Content-Type") String contentType) {
        System.out.println(contentType);
        return contentType;
    }

    /**
     * Here we will get the headers ourselves using the HttpServletRequest object. When we include this object in the
     * parameter list, Spring knows to pass it into the method for us. We can use it to get everything we want from the
     * request. We call the getHeader method and pass it the key. If it is present in the request, getHeader() will
     * return the value. If that header is not present (the specified key not found) getHeader() will return null.
     */
    @GetMapping("/getHeadersByName")
    public String readHeaders(HttpServletRequest request) {
        String contentType = "Content-Type: " + request.getHeader("Content-Type");
        System.out.println(contentType);
        return contentType;
    }

    /**
     * Here we will view all headers. The getHeaderNames() method returns an Enumeration, which is similar to a List.
     * This is a fairly common pattern when working with sets of key/value pairs. First we get an enumeration of the
     * keys, then use that enumeration of keys to get the values one by one in a loop. Similar to how Iterators work,
     * we check if there are more elements in the Enumeration at the top of the loop. If that returns true, we then
     * get that element inside the loop.
     */
    @GetMapping("/readAllHeaders")
    public String readAllHeaders(HttpServletRequest request) {
        StringBuilder stringBuilder = new StringBuilder("Headers:\n\t");


        Enumeration<String> keys = request.getHeaderNames();
        while(keys.hasMoreElements()) {
            String key = keys.nextElement();
            String value = request.getHeader(key);

            stringBuilder.append(key);
            stringBuilder.append(": ");
            stringBuilder.append(value);
            stringBuilder.append("\n\t");
        }

        System.out.println(stringBuilder.toString());
        return stringBuilder.toString();
    }

    /**
     * Here we are going to add headers to the response object. Adding headers to a response is easy, just call the
     * setHeader() method on the response object. Headers are key/value pairs of strings, so simply pass two strings to
     * the method: first the key, then the value. Note there is also an addHeader() method which can accomplish the
     * same thing but works a little differently.
     */
    @GetMapping("/writeHeaders")
    public String writeHeaders(HttpServletResponse response) {
        response.addHeader("Key-1", "Value 1");
        response.addHeader("Key-2", "Value 2");
        response.addHeader("Revature-Slogan", "Code like a boss");
        response.addHeader("Content-Type", "text/plain");
        response.addHeader("Cache-Seconds", "3600");
        return "Check the response headers!";
    }

    /**
     * Here we will use ResponseEntity to set the headers. This is a slightly different workflow to accomplish the
     * same thing as the writeHeaders() method above.
     */
    @GetMapping("/responseEntityHeaders")
    public ResponseEntity<String> writeHeadersWithResponseEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Key-1", "Value 1");
        headers.add("Key-2", "Value 2");
        headers.add("Revature-Slogan", "Code like a boss");
        headers.add("Content-Type", "text/plain");
        headers.add("Cache-Seconds", "3600");
        return new ResponseEntity<String>("Check the response headers!", headers, HttpStatus.OK);
    }
}
