package net.enjoy.springboot.registrationlogin.controller;

import lombok.RequiredArgsConstructor;
import net.enjoy.springboot.registrationlogin.service.RoomService;
import net.enjoy.springboot.registrationlogin.utils.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@RestController
@RequestMapping("/ws")
@RequiredArgsConstructor
public class WebSocketAuthController {
    private final RoomService roomService;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String userId = loginRequest.get("userId");
        String role = loginRequest.get("role");
        String password = loginRequest.get("password");
        String roomId = loginRequest.get("roomId");

        if (userId == null || !isValidRole(role)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid userId or role"));
        }

        // 生成token，payload: userId|role
        String token = jwtUtil.generateToken(userId + "|" + role);
        return ResponseEntity.ok(Map.of("token", token, "roomId", roomId));
    }

    @GetMapping("/isUserInRoom")
    public ResponseEntity<?> isUserInRoom(@RequestParam String userId, @RequestParam String roomId) {
        boolean exists = roomService.isUserInRoom(roomId, userId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    private boolean isValidRole(String role) {
        return role != null &&
                (role.equals("anchor") ||
                 role.equals("proxy") ||
                 role.equals("controller"));
    }
} 