package api.controller.users;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UsersController {

    private final JdbcTemplate jdbcTemplate;

    public UsersController(JdbcTemplate jdbcTemplate) {this.jdbcTemplate = jdbcTemplate;}

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> getAllUsers() {
        List<Map<String, Object>> users = jdbcTemplate.queryForList("SELECT id, username, balance FROM users");
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("users", users);
        return response;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getAllUsers(@PathVariable int userId) {
        List<Map<String, Object>> users = jdbcTemplate.queryForList("SELECT id, username, balance FROM users where id = ?", userId);
        Map<String, Object> response = new HashMap<>();
        if (users.size() == 0) {
            response.put("success", false);
            response.put("error", "Not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } else {
            response.put("success", true);
            response.put("user", users.get(0));
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody @Valid PostUsersRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            jdbcTemplate.update("INSERT INTO users (username, password, balance) VALUES (?, ?, ?)", request.getUsername(), request.getPassword(), 100);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Username already exists");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
        response.put("success", true);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
