package api.controller.users;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UsersController {

    @GetMapping("/hello")
    @ResponseStatus(HttpStatus.CREATED)
    public String hello() {
        return "Hello World!";
    }
}
