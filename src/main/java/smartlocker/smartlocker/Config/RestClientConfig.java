package smartlocker.smartlocker.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    @Bean
    RestClient restClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(60000); // 60 giây
        requestFactory.setReadTimeout(60000);    // 60 giây

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }
}

