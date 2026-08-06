package smartlocker.smartlocker.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.transaction.annotation.Transactional;
import smartlocker.smartlocker.dto.UpdateUserProfileRequest;
import smartlocker.smartlocker.dto.UserOrderSummaryDto;
import smartlocker.smartlocker.dto.UserProfileResponse;
import smartlocker.smartlocker.model.Order;
import smartlocker.smartlocker.model.OrderOtp;
import smartlocker.smartlocker.model.User;
import smartlocker.smartlocker.repository.OrderOtpRepository;
import smartlocker.smartlocker.repository.OrderRepository;
import smartlocker.smartlocker.repository.UserRepository;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserProfileController {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderOtpRepository orderOtpRepository;

    public UserProfileController(UserRepository userRepository,
                                  OrderRepository orderRepository,
                                  OrderOtpRepository orderOtpRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderOtpRepository = orderOtpRepository;
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<?> getProfile(@PathVariable UUID userId) {
        return userRepository.findById(userId)
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(toResponse(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{userId}/profile")
    public ResponseEntity<?> updateProfile(
            @PathVariable UUID userId,
            @RequestBody UpdateUserProfileRequest request) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        String fullName = request.fullName() == null ? "" : request.fullName().trim();
        String email = request.email() == null ? "" : request.email().trim().toLowerCase();
        String phone = request.phoneNumber() == null ? "" : request.phoneNumber().trim();
        if (fullName.isBlank() || phone.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Full name and phone number are required."));
        }
        if (!email.isBlank() && userRepository.findByEmail(email)
                .filter(existing -> !existing.getId().equals(userId)).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "This email address is already in use."));
        }
        if (userRepository.findByPhoneNumber(phone)
                .filter(existing -> !existing.getId().equals(userId)).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "This phone number is already in use."));
        }

        user.setFullName(fullName);
        user.setEmail(email.isBlank() ? null : email);
        user.setPhoneNumber(phone);
        return ResponseEntity.ok(toResponse(userRepository.save(user)));
    }

    /**
     * GET /api/users/{userId}/orders
     * Trả về danh sách đơn hàng người dùng nhận kèm OTP đang hiệu lực.
     */
    @Transactional(readOnly = true)
    @GetMapping("/{userId}/orders")
    public ResponseEntity<?> getUserOrders(@PathVariable UUID userId) {
        if (userRepository.findById(userId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Order> orders = orderRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId);
        List<UserOrderSummaryDto> result = orders.stream().map(order -> {
            // Tìm OTP còn hiệu lực cho đơn này
            OrderOtp activeOtp = orderOtpRepository
                    .findTopByOrderIdAndIsUsedFalseAndExpiredAtAfterOrderByCreatedAtDesc(
                            order.getId(), LocalDateTime.now())
                    .orElse(null);

            List<String> lockerCodes = order.getOrderLockers() == null ? List.of()
                    : order.getOrderLockers().stream()
                            .map(ol -> ol.getLocker() != null ? ol.getLocker().getLockerCode() : "?")
                            .collect(Collectors.toList());

            return new UserOrderSummaryDto(
                    order.getId(),
                    order.getStation() != null ? order.getStation().getName() : "Unknown",
                    order.getStation() != null ? order.getStation().getAddress() : "",
                    order.getStatus(),
                    order.getRecipientPhoneNumber(),
                    order.getCreatedAt(),
                    order.getExpiredAt(),
                    activeOtp != null ? activeOtp.getOtpCode() : null,
                    activeOtp != null ? activeOtp.getExpiredAt() : null,
                    lockerCodes
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(
                user.getId(), user.getFullName(), user.getEmail(), user.getPhoneNumber());
    }
}

