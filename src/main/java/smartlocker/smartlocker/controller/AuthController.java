package smartlocker.smartlocker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import smartlocker.smartlocker.dto.LoginRequest;
import smartlocker.smartlocker.dto.LoginResponse;
import smartlocker.smartlocker.model.User;
import smartlocker.smartlocker.repository.UserRepository;
import smartlocker.smartlocker.utils.JwtUtil;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Cho phép gọi API từ frontend React
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public AuthController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new LoginResponse(null, "Tên đăng nhập và mật khẩu không được để trống!"));
        }

        // Tìm kiếm người dùng trong cơ sở dữ liệu (qua email hoặc số điện thoại)
        Optional<User> userOpt;
        if (username.contains("@")) {
            userOpt = userRepository.findByEmail(username);
        } else {
            userOpt = userRepository.findByPhoneNumber(username);
        }

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // So khớp mật khẩu đã băm trong database
            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                String token = jwtUtil.generateToken(user.getId(), username, user.getFullName(), "USER");
                return ResponseEntity.ok(new LoginResponse(token, "Đăng nhập thành công!"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new LoginResponse(null, "Mật khẩu không chính xác!"));
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new LoginResponse(null, "Tài khoản không tồn tại trên hệ thống!"));
    }
}
