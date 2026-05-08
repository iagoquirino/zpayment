package com.java.seed;

import com.java.seed.user_order.UserOrderRepository;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.UUID;

import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

@ActiveProfiles("test")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTest {

    static final Network network = Network.newNetwork();

    static PostgreSQLContainer postgres;

    static ConfluentKafkaContainer kafka;

    static GenericContainer<?> schemaRegistry;

    @Autowired
    private UserOrderRepository userOrderRepository;

    static {
        postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:17"))
                .withNetwork(network)
                .withNetworkAliases("postgres")
                .withReuse(true);

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

        Startables.deepStart(postgres, kafka, schemaRegistry).join();
        System.setProperty("SCHEMA_REGISTRY_URL", "http://" + schemaRegistry.getHost() + ":" + schemaRegistry.getMappedPort(8081));
    }

    @BeforeEach
    public void setup() {
        userOrderRepository.deleteAll();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:postgresql://%s:%s/postgres".formatted(
                postgres.getHost(), postgres.getMappedPort(5432)));
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.producer.properties.schema.registry.url", () ->
                "http://%s:%s".formatted(schemaRegistry.getHost(), schemaRegistry.getMappedPort(8081)));
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
