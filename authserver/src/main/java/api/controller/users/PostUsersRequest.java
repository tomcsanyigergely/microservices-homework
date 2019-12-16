package api.controller.users;

import javax.validation.constraints.Size;
import lombok.Data;


@Data
public class PostUsersRequest {

    @Size(min = 6, max = 30)
    private final String username;

    @Size(min = 8, max = 30)
    private final String password;
}
