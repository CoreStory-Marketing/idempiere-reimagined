package com.corestory.idempiere.gateway.security;

import com.corestory.idempiere.gateway.config.GatewayProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Single hardcoded admin login endpoint. JWT issued on success.
 * <p>For demo only — real auth would integrate with an IdP.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final GatewayProperties props;

    public AuthController(JwtUtil jwtUtil, GatewayProperties props) {
        this.jwtUtil = jwtUtil;
        this.props = props;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest body) {
        if (!props.getDemoUser().getUsername().equals(body.username())
            || !props.getDemoUser().getPassword().equals(body.password())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Map.of("error", "INVALID_CREDENTIALS", "message", "Invalid username or password"));
        }
        String token = jwtUtil.issue(body.username());
        return ResponseEntity.ok(Map.of(
            "token", token,
            "tokenType", "Bearer",
            "expiresInMinutes", props.getJwt().getTtlMinutes(),
            "username", body.username()
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me() {
        return ResponseEntity.ok(Map.of(
            "username", props.getDemoUser().getUsername(),
            "roles", new String[]{"ADMIN"}
        ));
    }

    public record LoginRequest(String username, String password) {}
}
