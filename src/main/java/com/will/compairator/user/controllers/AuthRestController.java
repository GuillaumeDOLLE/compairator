package com.will.compairator.user.controllers;

import com.will.compairator.user.dto.UserDTO;
import com.will.compairator.user.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthRestController {
    private final UserService users;
    public AuthRestController(UserService users) { this.users = users; }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("headerName", token.getHeaderName(), "token", token.getToken());
    }

    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
    public UserDTO.Profile register(@Valid @RequestBody UserDTO.Registration input) {
        return users.register(input);
    }

    @GetMapping("/me")
    public UserDTO.Profile me(Principal principal) {
        return UserDTO.Profile.from(users.find(principal.getName()));
    }
}
