package api.controller.transactions;

import api.controller.SuccessResponse;
import api.exception.AlreadyProcessedException;
import api.exception.NotEnoughBalanceException;
import api.exception.NotFoundException;
import api.exception.UserHasNoAccountException;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
public class TransactionsController {

    private final JdbcTemplate jdbcTemplate;

    public TransactionsController(JdbcTemplate jdbcTemplate) {this.jdbcTemplate = jdbcTemplate;}

    @GetMapping("/transactions")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> getAllTransactions() {
        log.info("GET /transactions");
        List<Map<String, Object>> transactions = jdbcTemplate.queryForList("SELECT * FROM transactions");
        return new SuccessResponse().put("transactions", transactions).build();
    }

    @GetMapping("/transactions/{transactionId}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> getTransaction(@PathVariable String transactionId) throws NotFoundException {
        log.info("GET /transactions/" + transactionId);
        List<Map<String, Object>> transactions = jdbcTemplate.queryForList("SELECT * FROM transactions WHERE id = ?", transactionId);
        if (transactions.size() == 1) {
            return new SuccessResponse().put("transaction", transactions.get(0)).build();
        } else {
            throw new NotFoundException();
        }
    }

    @Transactional(rollbackFor = Exception.class) // rollback for checked exceptions too
    @PutMapping("/transactions/{transactionId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createTransaction(@PathVariable String transactionId, @RequestHeader("X-USERNAME") String username, @RequestBody int amount) throws NotEnoughBalanceException, UserHasNoAccountException, AlreadyProcessedException {
        log.info("PUT /transactions/" + transactionId);
        jdbcTemplate.update("LOCK TABLE accounts IN ACCESS EXCLUSIVE MODE");
        List<Map<String, Object>> records = jdbcTemplate.queryForList("SELECT balance FROM accounts WHERE username = ?", username);
        if (records.size() == 1) {
            try {
                jdbcTemplate.update("INSERT INTO transactions (id, amount, username) VALUES (?, ?, ?)", transactionId, amount, username);
            } catch (Exception e) {
                throw new AlreadyProcessedException();
            }
            int currentBalance = (int)records.get(0).get("balance");
            if (currentBalance >= amount) {
                jdbcTemplate.update("UPDATE accounts SET balance = balance - ? WHERE username = ?", amount, username);
                return new SuccessResponse().build();
            } else {
                throw new NotEnoughBalanceException();
            }
        } else {
            throw new UserHasNoAccountException();
        }
    }

    @Transactional(rollbackFor = Exception.class) // rollback for checked exceptions too
    @DeleteMapping("/transactions/{transactionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Map<String, Object> deleteTransaction(@PathVariable String transactionId) throws NotFoundException {
        log.info("DELETE /transactions/" + transactionId);
        jdbcTemplate.update("LOCK TABLE transactions IN ACCESS EXCLUSIVE MODE");
        List<Map<String, Object>> records = jdbcTemplate.queryForList("SELECT amount, username FROM transactions WHERE id = ?", transactionId);
        if (records.size() == 1) {
            Map<String, Object> transactionData = records.get(0);
            jdbcTemplate.update("UPDATE accounts SET balance = balance + ? WHERE username = ?", transactionData.get("amount"), transactionData.get("username"));
            jdbcTemplate.update("DELETE FROM transactions WHERE id = ?", transactionId);
            return new SuccessResponse().build();
        } else {
            throw new NotFoundException();
        }
    }
}
