package api.controller.authorization;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
public class AuthorizationController {

    private final JdbcTemplate jdbcTemplate;

    public AuthorizationController(JdbcTemplate jdbcTemplate) {this.jdbcTemplate = jdbcTemplate;}

    @GetMapping
    public ResponseEntity<Map<String, Object>> auth(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        final String authorization = request.getHeader("Authorization").trim();
        if (authorization != null && authorization.startsWith("Basic")) {
            // Authorization: Basic base64credentials
            String base64Credentials = authorization.substring("Basic".length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded, StandardCharsets.UTF_8);
            // credentials = username:password
            final String[] values = credentials.split(":", 2);

            int matching = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE username = ? AND password = ?", Integer.class, values[0], values[1]);


            if (matching == 1) {
                response.put("success", "true");
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        }
        response.put("success", "false");
        response.put("error", "Authentication failed");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
}
