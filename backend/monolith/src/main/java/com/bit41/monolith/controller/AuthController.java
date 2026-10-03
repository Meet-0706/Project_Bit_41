package com.bit41.monolith.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${jwt.secret}")
    private String jwtSecret;

    // In-memory users for demonstration matching the node.js version
    private final List<Map<String, String>> users = new ArrayList<>();

    public AuthController() {
        users.add(new HashMap<>(Map.of(
            "id", "00000000-0000-0000-0000-000000000000",
            "email", "admin@test.com",
            "password", "admin123",
            "role", "ADMIN"
        )));
        users.add(new HashMap<>(Map.of(
            "id", "11111111-1111-1111-1111-111111111111",
            "email", "customer@test.com",
            "password", "customer123",
            "role", "CUSTOMER"
        )));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> creds) {
        String email = creds.get("email");
        String password = creds.get("password");
        
        return users.stream()
            .filter(u -> u.get("email").equals(email) && u.get("password").equals(password)) // Simple check instead of bcrypt for now
            .findFirst()
            .map(u -> {
                String token = Jwts.builder()
                    .claim("id", u.get("id"))
                    .claim("role", u.get("role"))
                    .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                    .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .compact();
                
                return ResponseEntity.ok(Map.of("token", token, "user", u));
            })
            .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials")));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> creds) {
        String email = creds.get("email");
        String password = creds.get("password");
        
        if (users.stream().anyMatch(u -> u.get("email").equals(email))) {
            return ResponseEntity.badRequest().body(Map.of("error", "User already exists"));
        }
        
        Map<String, String> newUser = new HashMap<>(Map.of(
            "id", UUID.randomUUID().toString(),
            "email", email,
            "password", password,
            "role", "CUSTOMER"
        ));
        
        users.add(newUser);
        
        String token = Jwts.builder()
            .claim("id", newUser.get("id"))
            .claim("role", newUser.get("role"))
            .setExpiration(new Date(System.currentTimeMillis() + 3600000))
            .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
            .compact();
            
        return ResponseEntity.ok(Map.of("token", token, "user", newUser));
    }
}
