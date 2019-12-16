package api.controller.transactions;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;


@Data
public class TransactionRequest {

    @NotNull
    private final String request_id;

    @NotNull
    @Min(0)
    private final int id;

    @NotNull
    private final int amount;
}
