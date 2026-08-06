package smartlocker.smartlocker.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.*;
import org.springframework.web.multipart.MultipartFile;

import smartlocker.smartlocker.dto.FaceEnrollmentRequest;
import smartlocker.smartlocker.model.User;
import smartlocker.smartlocker.model.UserFaceEmbedding;
import smartlocker.smartlocker.repository.UserFaceEmbeddingRepo;
import smartlocker.smartlocker.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class FaceAuthRegService {
    private final VectorizorPngService vectorizorPngService;
    private final UserRepository userRepository;
    private final UserFaceEmbeddingRepo faceEmbeddingRepo;

    public FaceAuthRegService(VectorizorPngService vectorizorPngService,
            UserRepository userRepository, UserFaceEmbeddingRepo faceEmbeddingRepo) {
        this.vectorizorPngService = vectorizorPngService;
        this.userRepository = userRepository;
        this.faceEmbeddingRepo = faceEmbeddingRepo;
    }

    @Transactional
    public void faceAuthRegister(FaceEnrollmentRequest request) throws IOException {
        // 1. Tìm user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 2. Định nghĩa danh sách các góc mặt và file tương ứng

        List<FaceAngleTarget> targets = List.of(
                new FaceAngleTarget("front", request.getMidFace()),
                new FaceAngleTarget("left", request.getLeftFace()),
                new FaceAngleTarget("right", request.getRightFace()),
                new FaceAngleTarget("up", request.getUpFace()));

        List<CompletableFuture<ProcessedEmbedding>> futures = targets.stream()
                .filter(t -> t.file != null && !t.file.isEmpty())
                .map(t -> CompletableFuture.supplyAsync(() -> {
                    try {
                        float[] emb = vectorizorPngService.getEmbedding(t.file);
                        return new ProcessedEmbedding(t.angle, emb);
                    } catch (IOException e) {
                        throw new RuntimeException("Error vectorizing angle " + t.angle, e);
                    }
                }))
                .toList();

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        List<ProcessedEmbedding> results = allOf.thenApply(v -> futures.stream().map(CompletableFuture::join).toList())
                .join();

        // 3. Xoá tất cả các vector nhúng cũ của user này trước khi lưu mới
        faceEmbeddingRepo.deleteByUser(user);

        // 4. Lưu danh sách các vector nhúng mới
        for (ProcessedEmbedding res : results) {
            UserFaceEmbedding userFace = new UserFaceEmbedding();
            userFace.setUser(user);
            userFace.setEmbedding(res.embedding);
            userFace.setFaceAngle(res.angle);
            userFace.setCreatedAt(LocalDateTime.now());
            faceEmbeddingRepo.save(userFace);
        }
    }


}

class ProcessedEmbedding {
    String angle;
    float[] embedding;

    ProcessedEmbedding(String angle, float[] embedding) {
        this.angle = angle;
        this.embedding = embedding;
    }
}

class FaceAngleTarget {
    String angle;
    MultipartFile file;

    FaceAngleTarget(String angle, MultipartFile file) {
        this.angle = angle;
        this.file = file;
    }
}