package smartlocker.smartlocker.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import smartlocker.smartlocker.model.Order;
import smartlocker.smartlocker.model.OrderOtp;
import smartlocker.smartlocker.model.User;
import smartlocker.smartlocker.repository.OrderOtpRepository;
import smartlocker.smartlocker.repository.OrderRepository;

@Service
public class OrderOtpService {

    private static final Logger log = LoggerFactory.getLogger(OrderOtpService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int OTP_BOUND = 1_000_000;
    private static final int MAX_GENERATION_ATTEMPTS = 20;

    private final OrderRepository orderRepository;
    private final OrderOtpRepository orderOtpRepository;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final Duration otpLifetime;
    private final int maxAttempts;
    private final String mailFrom;

    public OrderOtpService(
            OrderRepository orderRepository,
            OrderOtpRepository orderOtpRepository,
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.otp.lifetime-minutes:10}") long lifetimeMinutes,
            @Value("${app.otp.max-attempts:3}") int maxAttempts,
            @Value("${spring.mail.username:}") String mailFrom) {
        if (lifetimeMinutes <= 0) {
            throw new IllegalArgumentException("app.otp.lifetime-minutes must be greater than zero");
        }
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("app.otp.max-attempts must be greater than zero");
        }
        this.orderRepository = orderRepository;
        this.orderOtpRepository = orderOtpRepository;
        this.mailSenderProvider = mailSenderProvider;
        this.otpLifetime = Duration.ofMinutes(lifetimeMinutes);
        this.maxAttempts = maxAttempts;
        this.mailFrom = mailFrom;
    }

    /**
     * Creates and persists a six-digit pickup OTP. Email delivery is best-effort:
     * missing mail configuration, a missing recipient email, or a delivery error
     * never prevents the OTP from being returned.
     */
    @Transactional
    public OrderOtp createOtp(UUID orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID is required.");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        LocalDateTime now = LocalDateTime.now();
        return orderOtpRepository
                .findTopByOrderIdAndIsUsedFalseAndExpiredAtAfterOrderByCreatedAtDesc(orderId, now)
                .orElseGet(() -> createAndSendOtp(order, now));
    }

    private OrderOtp createAndSendOtp(Order order, LocalDateTime now) {
        OrderOtp otp = new OrderOtp();
        otp.setOrder(order);
        otp.setOtpCode(generateUniqueCode());
        otp.setCreatedAt(now);
        otp.setExpiredAt(now.plus(otpLifetime));
        otp.setAttemptsCount(0);
        otp.setMaxAttempts(maxAttempts);
        otp.setIsUsed(false);

        OrderOtp savedOtp = orderOtpRepository.saveAndFlush(otp);
        trySendEmail(order, savedOtp);
        return savedOtp;
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String code = String.format("%06d", SECURE_RANDOM.nextInt(OTP_BOUND));
            if (!orderOtpRepository.existsByOtpCodeAndIsUsedFalse(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Could not generate a unique OTP. Please try again.");
    }

    private void trySendEmail(Order order, OrderOtp otp) {
        User recipient = order.getRecipientUser();
        String recipientEmail = recipient == null ? null : recipient.getEmail();
        if (recipientEmail == null || recipientEmail.isBlank()) {
            log.info("OTP {} for order {} was created, but no recipient email is available",
                    otp.getId(), order.getId());
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("OTP {} for order {} was created, but mail is not configured",
                    otp.getId(), order.getId());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (mailFrom != null && !mailFrom.isBlank()) {
                message.setFrom(mailFrom);
            }
            message.setTo(recipientEmail.trim());
            message.setSubject("Smart Locker pickup OTP");
            message.setText("Your pickup OTP is " + otp.getOtpCode()
                    + ". It expires at " + otp.getExpiredAt() + ".");
            mailSender.send(message);
        } catch (Exception exception) {
            log.warn("OTP {} for order {} was created, but email delivery failed: {}",
                    otp.getId(), order.getId(), exception.getMessage());
        }
    }
}
