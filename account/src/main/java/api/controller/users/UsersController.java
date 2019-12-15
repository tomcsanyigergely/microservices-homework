package api.controller.users;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UsersController {

    private final JdbcTemplate jdbcTemplate;

    public UsersController(JdbcTemplate jdbcTemplate) {this.jdbcTemplate = jdbcTemplate;}

    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> getAllUsers() {
        List<Map<String, Object>> users = jdbcTemplate.queryForList("SELECT id, username, balance FROM users");
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("users", users);
        return response;
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> getAllUsers(@PathVariable int userId) {
        List<Map<String, Object>> users = jdbcTemplate.queryForList("SELECT id, username, balance FROM users where id = ?", userId);
        if (users.size() == 0) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(users.get(0), HttpStatus.OK);
        }
    }
}
