package com.bankcore.auth.api;

import com.bankcore.auth.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/token")
    public ResponseEntity<Map<String, String>> issueToken(@Valid @RequestBody TokenRequest request) {
        String token = jwtService.generateToken(request.getUsername(), request.getRole());
        Map<String, String> body = new HashMap<>();
        body.put("accessToken", token);
        body.put("tokenType", "Bearer");
        return ResponseEntity.ok(body);
    }

    public static class TokenRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String role;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}
