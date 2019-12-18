package api.controller.accounts;


import api.controller.SuccessResponse;
import api.exception.AccountAlreadyCreatedException;
import api.exception.NotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
public class AccountController {

    private final JdbcTemplate jdbcTemplate;

    public AccountController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/accounts")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> getAllAccounts() {
        log.info("GET /accounts");
        List<Map<String, Object>> accounts = jdbcTemplate.queryForList("SELECT * FROM accounts");
        return new SuccessResponse().put("accounts", accounts).build();
    }

    @GetMapping("/accounts/{username}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> getAccount(@PathVariable String username) throws NotFoundException {
        log.info("GET /accounts/" + username);
        List<Map<String, Object>> accounts = jdbcTemplate.queryForList("SELECT * FROM accounts WHERE username = ?", username);
        if (accounts.size() == 1) {
            return new SuccessResponse().put("account", accounts.get(0)).build();
        } else {
            throw new NotFoundException();
        }
    }

    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createAccount(@RequestHeader("X-USERNAME") String username) throws AccountAlreadyCreatedException {
        log.info("POST /accounts");
        try {
            jdbcTemplate.update("INSERT INTO accounts (username, balance) VALUES (?, ?)", username, 100);
            return new SuccessResponse().build();
        } catch (Exception e) {
            throw new AccountAlreadyCreatedException();
        }
    }
}
