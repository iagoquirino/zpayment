package com.java.seed.user;

import com.java.seed.api.UsersApi;
import com.java.seed.api.model.UserRequest;
import com.java.seed.api.model.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserController implements UsersApi {

    private final UserService userService;

    @Override
    public ResponseEntity<UserResponse> createUser(UserRequest userRequest) throws Exception {
        UUID userId = userService.publish(userRequest);
        return ResponseEntity.accepted().body(new UserResponse()
                .userId(userId)
                .result(UserResponse.ResultEnum.SUCCESS));
    }
}
