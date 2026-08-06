
package smartlocker.smartlocker.service;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import smartlocker.smartlocker.dto.FaceMatchDto;
import smartlocker.smartlocker.model.User;
import smartlocker.smartlocker.repository.UserFaceEmbeddingRepo;
import smartlocker.smartlocker.repository.UserRepository;

/**
 * AuthenticateService
 */
@Service
public class FaceUnlockService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FaceUnlockService.class);
    private static final int EMBEDDING_SIZE = 512;
    private static final double DISTANCE_ACCEPT_THRESHOLD = 0.30;
    private final UserFaceEmbeddingRepo userFaceEmbeddingRepo;
    private final UserRepository userRepository;

    public FaceUnlockService(UserFaceEmbeddingRepo userFaceEmbeddingRepo, UserRepository userRepository) {
        this.userFaceEmbeddingRepo = userFaceEmbeddingRepo;
        this.userRepository = userRepository;
    }

    public Optional<User> findUserWithVector(UUID deviceId, float[] inputEmbed) {
        if (deviceId == null
                || inputEmbed == null
                || inputEmbed.length != EMBEDDING_SIZE
                || !isFinite(inputEmbed)) {
            LOGGER.warn(
                    "Face match skipped: deviceId={}, threshold={}, reason=invalid embedding",
                    deviceId,
                    DISTANCE_ACCEPT_THRESHOLD);
            return Optional.empty();
        }

        Optional<FaceMatchDto> bestMatch = userFaceEmbeddingRepo
                .findBestMatchDistance(deviceId, inputEmbed);

        if (bestMatch.isEmpty()) {
            LOGGER.info(
                    "Face match result: deviceId={}, candidate=none, threshold={}, accepted=false",
                    deviceId,
                    DISTANCE_ACCEPT_THRESHOLD);
            return Optional.empty();
        }

        FaceMatchDto match = bestMatch.get();
        Double distance = match.getDistance();
        boolean accepted = distance != null && distance <= DISTANCE_ACCEPT_THRESHOLD;
        LOGGER.info(
                "Face match result: deviceId={}, userId={}, distance={}, threshold={}, accepted={}",
                deviceId,
                match.getUserId(),
                distance,
                DISTANCE_ACCEPT_THRESHOLD,
                accepted);

        if (!accepted) {
            return Optional.empty();
        }
        return userRepository.findById(match.getUserId());
    }

    private boolean isFinite(float[] embedding) {
        for (float value : embedding) {
            if (!Float.isFinite(value)) {
                return false;
            }
        }
        return true;
    }
}
