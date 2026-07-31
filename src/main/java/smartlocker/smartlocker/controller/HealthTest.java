package smartlocker.smartlocker.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthTest {

    @GetMapping("/api/health")
    public String health() {
        return "Smart Locker Server is running";
    }
}