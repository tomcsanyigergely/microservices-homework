package api.controller.accounts;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class AccountController {

    @GetMapping("/accounts")
    @ResponseStatus(HttpStatus.OK)
    public List<Map<String, Object>> getAllAccounts() {}

    @PostMapping("/accounts")
    public void createAccount() {}
}
