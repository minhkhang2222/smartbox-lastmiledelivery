package smartlocker.smartlocker.service;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FaceRegistrationRateLimiter {

    // Lưu mốc thời gian đăng ký gần nhất của từng user
    private final Map<UUID, Instant> lastRegistrationTime = new ConcurrentHashMap<>();

    // Thời gian giãn cách tối thiểu giữa các lần đăng ký (giây)
    private static final long COOLDOWN_SECONDS = 30;

    public boolean isAllowed(UUID userId) {
        if (userId == null) {
            return true;
        }
        Instant now = Instant.now();
        Instant last = lastRegistrationTime.get(userId);
        if (last != null && now.isBefore(last.plusSeconds(COOLDOWN_SECONDS))) {
            return false;
        }
        lastRegistrationTime.put(userId, now);
        return true;
    }

    public long getRemainingCooldownSeconds(UUID userId) {
        if (userId == null) return 0;
        Instant last = lastRegistrationTime.get(userId);
        if (last == null) return 0;
        long elapsed = Instant.now().getEpochSecond() - last.getEpochSecond();
        return Math.max(0, COOLDOWN_SECONDS - elapsed);
    }
}
