package api.controller.users;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class UsersController {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public UsersController(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @RequestMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public void checkCredentials(HttpServletResponse response, Principal principal) {
        log.info("GET /");
        response.setHeader("X-USERNAME", principal.getName());
    }

    @PostMapping("/users")
    @Transactional
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody @Valid PostUsersRequest request) {
        log.info("POST /users");
        Map<String, Object> response = new HashMap<>();
        try {
            jdbcTemplate.update("INSERT INTO users (username, password) VALUES (?, ?)", request.getUsername(), passwordEncoder.encode(request.getPassword()));
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Username already exists");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
        response.put("success", true);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
