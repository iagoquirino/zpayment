package com.java.fraud;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

@ActiveProfiles("test")
@Testcontainers
@AutoConfigureRestTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTest {

    static final Network network = Network.newNetwork();

    static ConfluentKafkaContainer kafka;

    static GenericContainer<?> schemaRegistry;

    static GenericContainer<?> wireMock;

    static {
        kafka = new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.8.0"))
                .withNetwork(network)
                .withNetworkAliases("kafka")
                .withReuse(true);

        schemaRegistry = new GenericContainer<>(DockerImageName.parse("confluentinc/cp-schema-registry:7.8.0"))
                .withNetwork(network)
                .withExposedPorts(8081)
                .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://kafka:9093")
                .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
                .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081")
                .dependsOn(kafka)
                .withReuse(true)
                .waitingFor(Wait.forHttp("/subjects").forStatusCode(200));

        wireMock = new GenericContainer<>(DockerImageName.parse("wiremock/wiremock:latest"))
                .withNetwork(network)
                .withExposedPorts(8080)
                .withCommand("/docker-entrypoint.sh --global-response-templating --disable-gzip --verbose")
                .withFileSystemBind(Paths.get("../wiremock").toAbsolutePath().toString(), "/home/wiremock", BindMode.READ_ONLY)
                .waitingFor(Wait.forHttp("/__admin/health").forStatusCode(200));

        Startables.deepStart(kafka, schemaRegistry, wireMock).join();
        System.setProperty("SCHEMA_REGISTRY_URL", "http://" + schemaRegistry.getHost() + ":" + schemaRegistry.getMappedPort(8081));
    }

    @Autowired
    protected RestTestClient restTestClient;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.producer.properties.schema.registry.url", () ->
                "http://%s:%s".formatted(schemaRegistry.getHost(), schemaRegistry.getMappedPort(8081)));
        registry.add("client.fraud-checker.url", () ->
                "http://%s:%s".formatted(wireMock.getHost(), wireMock.getMappedPort(8080)));
    }


    public static <K, V> KafkaConsumer<K, V> kafkaConsumer() {
        return new KafkaConsumer<>(Map.of(
                BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                GROUP_ID_CONFIG, UUID.randomUUID().toString(),
                AUTO_OFFSET_RESET_CONFIG, "earliest",
                KEY_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class,
                VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class,
                "schema.registry.url", "http://%s:%s".formatted(schemaRegistry.getHost(), schemaRegistry.getMappedPort(8081)),
                "specific.avro.reader", "true"
        ));
    }
}
