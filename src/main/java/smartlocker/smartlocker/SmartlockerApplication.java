package smartlocker.smartlocker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartlockerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartlockerApplication.class, args);
	}

}
