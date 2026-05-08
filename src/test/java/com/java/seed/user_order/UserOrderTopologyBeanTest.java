package com.java.seed.user_order;

import com.java.seed.TopologyTestDriverTest;
import com.java.seed.order.events.OrderEvent;
import com.java.seed.order.events.OrderEventKey;
import com.java.seed.user.events.UserEvent;
import com.java.seed.user.events.UserEventKey;
import com.java.seed.user_order.events.UserOrderEvent;
import com.java.seed.user_order.events.UserOrderEventKey;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.test.TestRecord;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static com.java.seed.shared.TestUtils.givenOrderEvent;
import static com.java.seed.shared.TestUtils.givenUserEvent;
import static org.assertj.core.api.Assertions.assertThat;

class UserOrderTopologyBeanTest extends TopologyTestDriverTest {

    private static final String USER_TOPIC = "user_topic";
    private static final String ORDER_TOPIC = "order_topic";

    private final Serde<UserEventKey> userEventKeySerde = serde(true);
    private final Serde<UserEvent> userEventSerde = serde(false);
    private TestInputTopic<UserEventKey, UserEvent> userTopic;

    private final Serde<OrderEventKey> orderEventKeySerde = serde(true);
    private final Serde<OrderEvent> orderEventSerde = serde(false);
    private TestInputTopic<OrderEventKey, OrderEvent> orderTopic;

    private final Serde<UserOrderEventKey> userOrderEventKeySerde = serde(true);
    private final Serde<UserOrderEvent> userOrderEventSerde = serde(false);
    private TestOutputTopic<UserOrderEventKey, UserOrderEvent> userOrderTopic;

    private static final String OUTPUT_TOPIC = "user_order_topic";

    @Override
    public StreamsBuilder setupStreams() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KTable<UserEventKey, UserEvent> userEventKTable = streamsBuilder.stream(USER_TOPIC, Consumed.with(userEventKeySerde, userEventSerde))
                .toTable();
        KStream<OrderEventKey, OrderEvent> orderEventKStream = streamsBuilder.stream(ORDER_TOPIC, Consumed.with(orderEventKeySerde, orderEventSerde));

        new UserOrderTopologyBean()
                .userOrderTopology()
                .apply(userEventKTable, orderEventKStream)
                .to(OUTPUT_TOPIC, Produced.with(userOrderEventKeySerde, userOrderEventSerde));

        return streamsBuilder;
    }

    @Override
    public void setupTopology() {
        userTopic = testDriver.createInputTopic(USER_TOPIC, userEventKeySerde.serializer(), userEventSerde.serializer());
        orderTopic = testDriver.createInputTopic(ORDER_TOPIC, orderEventKeySerde.serializer(), orderEventSerde.serializer());
        userOrderTopic = testDriver.createOutputTopic(OUTPUT_TOPIC, userOrderEventKeySerde.deserializer(), userOrderEventSerde.deserializer());
    }

    @Test
    void shouldJoinUserAndOrderEvents() {
        // given
        UUID userId = UUID.randomUUID();
        UserEvent user = givenUserEvent(userId);

        UUID orderId = UUID.randomUUID();
        OrderEvent order = givenOrderEvent(orderId, userId);

        //when
        userTopic.pipeInput(new UserEventKey(userId), user);
        orderTopic.pipeInput(new OrderEventKey(orderId), order);

        // then
        List<TestRecord<UserOrderEventKey, UserOrderEvent>> events = userOrderTopic.readRecordsToList();

        assertThat(events.isEmpty()).isFalse();

        UserOrderEvent result = events.stream().findFirst().get().getValue();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getOrderId()).isEqualTo(orderId);
        assertThat(result.getName().toString()).isEqualTo("John Doe");
    }

    @Test
    void shouldNotReturnOnlyUserTopicPublished() {
        // given
        UUID userId = UUID.randomUUID();
        UserEvent user = givenUserEvent(userId);

        //when
        userTopic.pipeInput(new UserEventKey(userId), user);

        // then
        List<TestRecord<UserOrderEventKey, UserOrderEvent>> events = userOrderTopic.readRecordsToList();

        assertThat(events.isEmpty()).isTrue();
    }

    @Test
    void shouldNotReturnOnlyOrderTopicPublished() {
        // given
        UUID userId = UUID.randomUUID();

        UUID orderId = UUID.randomUUID();
        OrderEvent order = givenOrderEvent(orderId, userId);

        //when
        orderTopic.pipeInput(new OrderEventKey(orderId), order);

        // then
        List<TestRecord<UserOrderEventKey, UserOrderEvent>> events = userOrderTopic.readRecordsToList();

        assertThat(events.isEmpty()).isTrue();
    }
}
