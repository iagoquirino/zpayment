package com.java.seed;

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TopologyTestDriver;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public abstract class TopologyTestDriverTest {

    public TopologyTestDriver testDriver;

    private final MockSchemaRegistryClient mockSchemaRegistryClient = new MockSchemaRegistryClient();

    @BeforeEach
    public void setup() {
        StreamsBuilder streamsBuilder = this.setupStreams();
        testDriver = new TopologyTestDriver(streamsBuilder.build(), getProperties());
        this.setupTopology();
    }

    public Serde serde(boolean isKey) {
        SpecificAvroSerde specificAvroSerde = new SpecificAvroSerde<>(mockSchemaRegistryClient);
        specificAvroSerde.configure(Map.of("schema.registry.url", "mock://test"), isKey);
        return specificAvroSerde;
    }


    protected abstract StreamsBuilder setupStreams();

    protected abstract void setupTopology();

    @AfterEach
    void tearDown() {
        testDriver.close();
    }

    private static @NotNull Properties getProperties() {
        Properties props = new Properties();
        String applicationId = UUID.randomUUID().toString();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        props.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kstream-%s".formatted(applicationId));
        props.put("schema.registry.url", "mock://test");
        return props;
    }
}
