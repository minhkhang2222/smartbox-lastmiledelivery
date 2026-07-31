package smartlocker.smartlocker.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    private final String SECRET_KEY = "jpwjefpwe2342390tu3t";
    private final Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
    private final long EXPIRATION_TIME = 3600000; // 1 hour in ms

    public String generateToken(java.util.UUID userId, String username, String fullName, String role) {
        return JWT.create()
                .withSubject(username)
                .withClaim("id", userId.toString())
                .withClaim("name", fullName)
                .withClaim("role", role)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(algorithm);
    }
}
