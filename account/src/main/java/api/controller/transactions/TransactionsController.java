/*package api.controller.transactions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/transactions")
public class TransactionsController {

    private final JdbcTemplate jdbcTemplate;

    public TransactionsController(JdbcTemplate jdbcTemplate) {this.jdbcTemplate = jdbcTemplate;}

    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> createTransaction(@RequestBody @Valid TransactionRequest transactionRequest) throws InterruptedException {
        Map<String, Object> response = new HashMap<>();
        try {
            jdbcTemplate.update("INSERT INTO transactions (id, amount, user_id) VALUES (?, ?, ?)", transactionRequest.getRequest_id(), transactionRequest.getAmount(), transactionRequest.getId());

            jdbcTemplate.update("LOCK TABLE users IN ACCESS EXCLUSIVE MODE");
            int currentBalance = (int) jdbcTemplate.queryForList("SELECT balance FROM users WHERE id = ?", transactionRequest.getId()).get(0).get("balance");
            if (currentBalance >= transactionRequest.getAmount()) {
                Thread.sleep(6000);
                jdbcTemplate.update("UPDATE users SET balance = balance - ? WHERE id = ?", transactionRequest.getAmount(), transactionRequest.getId());
                response.put("success", true);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("success", false);
                response.put("error", "Not enough balance");
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Already processed");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
    }
}
*/