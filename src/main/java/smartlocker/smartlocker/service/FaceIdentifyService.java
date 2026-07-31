
package smartlocker.smartlocker.service;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

import smartlocker.smartlocker.model.User;
import smartlocker.smartlocker.repository.UserFaceEmbeddingRepo;
import smartlocker.smartlocker.repository.UserRepository;

/**
 * AuthenticateService
 */
@Service
public class FaceIdentifyService {
    private double distanceAcceptThreshold = 0.30;
    private final UserFaceEmbeddingRepo userFaceEmbeddingRepo;
    private final UserRepository userRepository;

    public FaceIdentifyService(UserFaceEmbeddingRepo userFaceEmbeddingRepo, UserRepository userRepository) {
        this.userFaceEmbeddingRepo = userFaceEmbeddingRepo;
        this.userRepository = userRepository;
    }

    public Optional<User> findUserWithVector(UUID deviceId, float[] inputEmbed) {
        if (deviceId == null
                || inputEmbed == null
                || inputEmbed.length != 512) {
            return Optional.empty();
        }

        return userFaceEmbeddingRepo
                .findBestMatchDistance(deviceId, inputEmbed)
                .filter(match -> match.getDistance() != null
                        && match.getDistance() <= distanceAcceptThreshold)
                .flatMap(match -> userRepository.findById(
                        match.getUserId()));
    }
}
