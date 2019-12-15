package api.controller;

import java.util.List;
import lombok.Data;

@Data
public class ErrorResponse {
    private final List<String> errors;
}
