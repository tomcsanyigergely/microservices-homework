package api.controller.transactions;

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
    public ResponseEntity<Map<String, Object>> createTransaction(@RequestBody @Valid TransactionRequest transactionRequest, HttpServletRequest request) {

        return new ResponseEntity<>(null, HttpStatus.OK);
    }
}
