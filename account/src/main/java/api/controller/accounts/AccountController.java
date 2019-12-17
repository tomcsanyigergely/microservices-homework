package api.controller.accounts;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        Map<String, Object> response = new HashMap<>();

        List<Map<String, Object>> accounts = jdbcTemplate.queryForList("SELECT * FROM accounts");

        response.put("success", true);
        response.put("accounts", accounts);
        return response;
    }

    @GetMapping("/accounts/{username}")
    public ResponseEntity<Map<String, Object>> getAccount(@PathVariable String username) {
        Map<String, Object> response = new HashMap<>();

        List<Map<String, Object>> accounts = jdbcTemplate.queryForList("SELECT * FROM accounts WHERE username = ?", username);
        if (accounts.size() == 1) {
            response.put("success", true);
            response.put("accounts", accounts);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.put("success", false);
            response.put("error", "Account not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/accounts")
    public ResponseEntity<Map<String, Object>> createAccount(@RequestHeader("X-USERNAME") String username) {
        log.info("X-USERNAME header received: " + username);

        Map<String, Object> response = new HashMap<>();

        try {
            jdbcTemplate.update("INSERT INTO accounts (username, balance) VALUES (?, ?)", username, 100);
            response.put("success", true);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Account already created");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
    }
}
