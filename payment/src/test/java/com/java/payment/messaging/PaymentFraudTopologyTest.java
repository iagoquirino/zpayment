package com.java.payment.messaging;

import com.java.fraud.events.FraudPaymentEvent;
import com.java.fraud.events.FraudPaymentKey;
import com.java.fraud.events.FraudPaymentResultEnum;
import com.java.payment.TopologyTestDriverTest;
import com.java.payment.events.*;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.test.TestRecord;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static com.java.payment.TestUtils.givenFraudPaymentEvent;
import static com.java.payment.TestUtils.givenPaymentEvent;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentFraudTopologyTest extends TopologyTestDriverTest {

    private static final String PAYMENT_TOPIC = "payment_submission_topic";
    private static final String FRAUD_TOPIC = "fraud_process_topic";

    private final Serde<PaymentKey> paymentKeySerde = serde(true);
    private final Serde<PaymentEvent> paymentEventSerde = serde(false);

    private TestInputTopic<PaymentKey, PaymentEvent> paymentTopic;

    private final Serde<FraudPaymentKey> fraudPaymentKeySerde = serde(true);
    private final Serde<FraudPaymentEvent> fraudPaymentEventSerde = serde(false);
    private TestInputTopic<FraudPaymentKey, FraudPaymentEvent> fraudTopic;

    private final Serde<PaymentProcessResultKey> paymentProcessResultKeySerde = serde(true);
    private final Serde<PaymentProcessResultEvent> paymentProcessResultSerde = serde(false);
    private TestOutputTopic<PaymentProcessResultKey, PaymentProcessResultEvent> paymentProcessResultOutputTopic;

    private static final String OUTPUT_TOPIC = "payment_processor_topic";

    @Override
    public StreamsBuilder setupStreams() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KStream<PaymentKey, PaymentEvent> paymentKStream = streamsBuilder.stream(PAYMENT_TOPIC, Consumed.with(paymentKeySerde, paymentEventSerde));
        KStream<FraudPaymentKey, FraudPaymentEvent> orderEventKStream = streamsBuilder.stream(FRAUD_TOPIC, Consumed.with(fraudPaymentKeySerde, fraudPaymentEventSerde));

        new PaymentFraudTopology()
                .paymentFraudTopologyV1()
                .apply(paymentKStream, orderEventKStream)
                .to(OUTPUT_TOPIC, Produced.with(paymentProcessResultKeySerde, paymentProcessResultSerde));

        return streamsBuilder;
    }

    @Override
    public void setupTopology() {
        paymentTopic = testDriver.createInputTopic(PAYMENT_TOPIC, paymentKeySerde.serializer(), paymentEventSerde.serializer());
        fraudTopic = testDriver.createInputTopic(FRAUD_TOPIC, fraudPaymentKeySerde.serializer(), fraudPaymentEventSerde.serializer());
        paymentProcessResultOutputTopic = testDriver.createOutputTopic(OUTPUT_TOPIC, paymentProcessResultKeySerde.deserializer(), paymentProcessResultSerde.deserializer());
    }

    @Test
    void shouldResultInUnderReviewWithNoFraudEvent() {
        // given

        PaymentEvent event = givenPaymentEvent();

        PaymentKey key = PaymentKey.newBuilder()
                .setPaymentId(event.getPaymentId())
                .build();

        //when
        paymentTopic.pipeInput(key, event);

        // then
        List<TestRecord<PaymentProcessResultKey, PaymentProcessResultEvent>> events = paymentProcessResultOutputTopic.readRecordsToList();

        assertThat(events.isEmpty()).isFalse();

        PaymentProcessResultEvent result = events.getLast().getValue();
        assertThat(result.getPaymentId()).isEqualTo(event.getPaymentId());
        assertThat(result.getProcessResult()).isEqualTo(PaymentProcessResultEnum.UNDER_REVIEW);
    }

    @Test
    void shouldResultInPassWithFraudEvent() {
        // given

        UUID paymentId = UUID.randomUUID();
        PaymentEvent event = givenPaymentEvent();
        event.setPaymentId(paymentId);

        PaymentKey key = PaymentKey.newBuilder()
                .setPaymentId(event.getPaymentId())
                .build();


        FraudPaymentEvent fraudEvent = givenFraudPaymentEvent();
        fraudEvent.setPaymentId(paymentId);

        FraudPaymentKey keyFraud = FraudPaymentKey.newBuilder()
                .setPaymentId(fraudEvent.getPaymentId())
                .setFraudId(fraudEvent.getPaymentId())
                .build();

        //when
        paymentTopic.pipeInput(key, event);
        fraudTopic.pipeInput(keyFraud, fraudEvent);

        // then
        List<TestRecord<PaymentProcessResultKey, PaymentProcessResultEvent>> events = paymentProcessResultOutputTopic.readRecordsToList();

        assertThat(events).hasSize(2);

        PaymentProcessResultEvent result = events.getLast().getValue();
        assertThat(result.getPaymentId()).isEqualTo(event.getPaymentId());
        assertThat(result.getProcessResult()).isEqualTo(PaymentProcessResultEnum.APPROVED);
    }

    @Test
    void shouldResultInRejectedWithFraudEvent() {
        // given

        UUID paymentId = UUID.randomUUID();
        PaymentEvent event = givenPaymentEvent();
        event.setPaymentId(paymentId);

        PaymentKey key = PaymentKey.newBuilder()
                .setPaymentId(event.getPaymentId())
                .build();


        FraudPaymentEvent fraudEvent = givenFraudPaymentEvent();
        fraudEvent.setPaymentId(paymentId);
        fraudEvent.setResult(FraudPaymentResultEnum.REJECT);

        FraudPaymentKey keyFraud = FraudPaymentKey.newBuilder()
                .setPaymentId(fraudEvent.getPaymentId())
                .setFraudId(fraudEvent.getPaymentId())
                .build();

        //when
        paymentTopic.pipeInput(key, event);
        fraudTopic.pipeInput(keyFraud, fraudEvent);

        // then
        List<TestRecord<PaymentProcessResultKey, PaymentProcessResultEvent>> events = paymentProcessResultOutputTopic.readRecordsToList();

        assertThat(events).hasSize(2);

        PaymentProcessResultEvent result = events.getLast().getValue();
        assertThat(result.getPaymentId()).isEqualTo(event.getPaymentId());
        assertThat(result.getProcessResult()).isEqualTo(PaymentProcessResultEnum.REJECTED);
    }
}
