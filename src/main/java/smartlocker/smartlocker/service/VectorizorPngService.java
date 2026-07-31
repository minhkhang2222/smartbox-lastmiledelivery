package smartlocker.smartlocker.service;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import smartlocker.smartlocker.dto.FaceResponse;

@Service
public class VectorizorPngService {

    private final RestClient restClient;

    public VectorizorPngService(RestClient restClient) {
        this.restClient = restClient;
    }

    public float[] getEmbedding(MultipartFile image) throws IOException {

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        body.add(
                "image",
                image.getResource());

        try {
            FaceResponse response = restClient.post()
                    .uri("http://localhost:9001/vectorize")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(FaceResponse.class);

            return response.getEmbedding();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Face service failed",
                    e);
        }
    }
}
