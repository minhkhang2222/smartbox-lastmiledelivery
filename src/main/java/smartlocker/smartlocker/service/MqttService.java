package smartlocker.smartlocker.service;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Consumer;

@Service
public class MqttService {

    @Value("${mqtt.server-uri}")
    private String host;

    @Value("${mqtt.port}")
    private int port;

    @Value("${mqtt.username}")
    private String username;

    @Value("${mqtt.password}")
    private String password;

    private Mqtt3AsyncClient client;

    @PostConstruct
    public void init() {
        var clientBuilder = MqttClient.builder()
                .useMqttVersion3()
                .identifier("smartlocker-backend-" + UUID.randomUUID())
                .serverHost(host)
                .serverPort(port)
                .sslWithDefaultConfig(); // MQTT secure broker (over TLS)

        if (username != null && !username.trim().isEmpty() && !"your_mqtt_username".equals(username)) {
            clientBuilder.simpleAuth()
                    .username(username)
                    .password((password != null ? password : "").getBytes(StandardCharsets.UTF_8))
                    .applySimpleAuth();
        }

        client = clientBuilder
                .automaticReconnectWithDefaultConfig()
                .addConnectedListener(context -> {
                    System.out.println("MQTT Connected/Reconnected.");
                })
                .buildAsync();

        client.connect()
                .whenComplete((connAck, throwable) -> {
                    if (throwable != null) {
                        System.err.println("MQTT Connection failed: " + throwable.getMessage());
                    } else {
                        System.out.println("MQTT Connected successfully to: " + host);
                    }
                });
    }

    public void publish(String topic, String payload) {
        if (client == null) {
            System.err.println("MQTT client not initialized.");
            return;
        }
        client.publishWith()
                .topic(topic)
                .payload(payload.getBytes(StandardCharsets.UTF_8))
                .send()
                .whenComplete((publishResult, throwable) -> {
                    if (throwable != null) {
                        System.err.println("Failed to publish to topic " + topic + ": " + throwable.getMessage());
                    } else {
                        System.out.println("Published to topic " + topic + ": " + payload);
                    }
                });
    }

    public void subscribe(String topic, Consumer<String> callback) {
        subscribe(topic, (actualTopic, payload) -> callback.accept(payload));
    }

    public void subscribe(String topic, java.util.function.BiConsumer<String, String> callback) {
        if (client == null) {
            System.err.println("MQTT client not initialized.");
            return;
        }
        client.subscribeWith()
                .topicFilter(topic)
                .callback(publish -> {
                    String payload = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
                    callback.accept(publish.getTopic().toString(), payload);
                })
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        System.err.println("Failed to subscribe to topic " + topic + ": " + throwable.getMessage());
                    } else {
                        System.out.println("Subscribed to topic: " + topic);
                    }
                });
    }

    @PreDestroy
    public void destroy() {
        if (client != null) {
            client.disconnect();
        }
    }
}
