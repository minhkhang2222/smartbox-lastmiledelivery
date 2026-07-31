
package smartlocker.smartlocker.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import smartlocker.smartlocker.dto.FaceMatchDto;
import smartlocker.smartlocker.model.User;
import smartlocker.smartlocker.model.UserFaceEmbedding;
import smartlocker.smartlocker.repository.UserFaceEmbeddingRepo;
import smartlocker.smartlocker.repository.UserRepository;

/**
 * AuthenticateService
 */
@Service
public class FaceUnlockService {
    private double distanceAcceptThreshold = 0.30;
    private final UserFaceEmbeddingRepo userFaceEmbeddingRepo;
    private final UserRepository userRepository;

    public FaceUnlockService(UserFaceEmbeddingRepo userFaceEmbeddingRepo, UserRepository userRepository) {
        this.userFaceEmbeddingRepo = userFaceEmbeddingRepo;
        this.userRepository = userRepository;
    }

    public Optional<User> findUserWithVector(UUID deviceCode, float[] inputEmbed) {
        if (deviceCode == null
                || inputEmbed == null
                || inputEmbed.length != 512) {
            return Optional.empty();
        }

        return userFaceEmbeddingRepo
                .findBestMatchDistance(deviceCode, inputEmbed)
                .filter(match -> match.getDistance() != null
                        && match.getDistance() <= distanceAcceptThreshold)
                .flatMap(match -> userRepository.findById(
                        match.getUserId()));
    }
}
