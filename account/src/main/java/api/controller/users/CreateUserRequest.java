package api.controller.users;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
class CreateUserRequest {

    @NotNull(message = "Missing username field")
    @Size(min = 3, message = "Username must contain at least 3 characters")
    @Size(max = 30, message = "Username can contain at most 30 characters")
    private String username;

    @NotNull(message = "Missing password field")
    @Size(min = 8, message = "Password must contain at least 8 characters")
    @Size(max = 30, message = "Username can contain at most 30 characters")
    private String password;
}
